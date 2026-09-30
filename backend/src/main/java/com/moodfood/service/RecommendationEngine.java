package com.moodfood.service;

import com.moodfood.domain.*;
import com.moodfood.repository.RecommendationHistoryRepository;
import com.moodfood.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Explicit weighted scoring engine (spec §13-§15) — no ML model. Weights are
 * versioned ({@link #ALGORITHM_VERSION}) so recommendation_history rows can
 * be compared across future weight changes (spec §28).
 */
@Service
public class RecommendationEngine {

  public static final String ALGORITHM_VERSION = "v1";

  private static final double W_MOOD = 0.30;
  private static final double W_TASTE = 0.25;
  private static final double W_SITUATION = 0.15;
  private static final double W_COOKING = 0.10;
  private static final double W_TIME = 0.10;
  private static final double W_BUDGET = 0.05;
  private static final double W_NOVELTY = 0.05;

  private static final double PERSONALIZATION_MAX_SWING = 8.0;

  private final RecipeRepository recipeRepository;
  private final RecommendationHistoryRepository historyRepository;
  private final FeedbackService feedbackService;

  public RecommendationEngine(RecipeRepository recipeRepository, RecommendationHistoryRepository historyRepository,
                               FeedbackService feedbackService) {
    this.recipeRepository = recipeRepository;
    this.historyRepository = historyRepository;
    this.feedbackService = feedbackService;
  }

  /** Ranks every published recipe for this context and returns the single best
   * one not already shown to this session very recently (spec §15 diversity —
   * if every candidate has been shown recently, the least-recently-shown one
   * wins rather than erroring on a small catalog). */
  @Transactional(readOnly = true)
  public Optional<ScoredRecipe> recommendOne(UUID sessionId, UserContext context) {
    Map<String, Double> preference = feedbackService.loadPreference(sessionId);
    List<Recipe> candidates = recipeRepository.findByQualityStatus(QualityStatus.PUBLISHED);
    if (candidates.isEmpty()) return Optional.empty();

    Map<Long, Instant> lastShownByRecipe = new HashMap<>();
    for (RecommendationHistory h : historyRepository.findBySessionIdAndShownAtAfter(
        sessionId, Instant.now().minus(7, ChronoUnit.DAYS))) {
      lastShownByRecipe.merge(h.getRecipeId(), h.getShownAt(), (a, b) -> a.isAfter(b) ? a : b);
    }

    List<ScoredRecipe> scored = new ArrayList<>();
    for (Recipe recipe : candidates) {
      if (recipe.getScores() == null) continue; // not tagged yet — not eligible
      scored.add(score(recipe, context, lastShownByRecipe.get(recipe.getId()), preference));
    }
    if (scored.isEmpty()) return Optional.empty();

    scored.sort(Comparator.comparing(ScoredRecipe::finalScore).reversed());
    return Optional.of(diversify(scored));
  }

  private ScoredRecipe score(Recipe recipe, UserContext context, Instant lastShownAt, Map<String, Double> preference) {
    RecipeScores s = recipe.getScores();

    int moodFit = s.moodScoreFor(context.mood());
    int tasteFit = s.tasteScoreFor(context.desiredTaste());
    int situationFit = situationFit(recipe, context.situation());
    int cookingFit = cookingFit(recipe, context.cookingWillingness());
    int timeFit = timeFit(recipe, context.timeAvailableMin());
    int budgetFit = 70; // no cost data yet — neutral placeholder, see class javadoc
    int novelty = novelty(lastShownAt);

    double weighted = moodFit * W_MOOD + tasteFit * W_TASTE + situationFit * W_SITUATION
        + cookingFit * W_COOKING + timeFit * W_TIME + budgetFit * W_BUDGET + novelty * W_NOVELTY;

    // Personalization (spec §17): "Base Score + Personal Preference" — an
    // additive nudge on top of the base weighted score, not a replacement of
    // any single dimension, capped so a strong learned preference can tilt
    // ranking but never overrule an outright mood/taste mismatch.
    double personalization = personalizationBonus(s, preference);

    String reason = RecommendationReasonGenerator.generate(context, recipe.getTitle());

    return new ScoredRecipe(recipe, BigDecimal.valueOf(weighted + personalization).setScale(2, RoundingMode.HALF_UP),
        moodFit, tasteFit, situationFit, cookingFit, timeFit, budgetFit, novelty, reason);
  }

  private double personalizationBonus(RecipeScores s, Map<String, Double> preference) {
    if (preference.isEmpty()) return 0;
    Map<String, Integer> recipeDims = Map.of(
        "tasteSpicy", s.getTasteSpicy(), "tasteSweet", s.getTasteSweet(),
        "tasteSavory", s.getTasteSavory(), "tasteRich", s.getTasteRich(),
        "tasteLight", s.getTasteLight(), "moodComfort", s.getMoodComfort(),
        "moodStressRelief", s.getMoodStressRelief(), "moodEnergy", s.getMoodEnergy(),
        "moodNostalgia", s.getMoodNostalgia());

    double weightedAlignment = 0;
    int n = 0;
    for (var entry : recipeDims.entrySet()) {
      Double pref = preference.get(entry.getKey());
      if (pref == null) continue;
      // alignment in [-1, 1]: both high (liked & recipe strong in it) -> +1-ish;
      // preference low but recipe strong in it -> negative.
      double alignment = ((pref - 50) / 50.0) * ((entry.getValue() - 50) / 50.0);
      weightedAlignment += alignment;
      n++;
    }
    if (n == 0) return 0;
    return (weightedAlignment / n) * PERSONALIZATION_MAX_SWING;
  }

  private int situationFit(Recipe recipe, Situation desired) {
    if (desired == null) return 70;
    boolean matches = recipe.getSituations().stream().anyMatch(t -> t.getSituation() == desired);
    return matches ? 100 : 40;
  }

  private int cookingFit(Recipe recipe, CookingWillingness willingness) {
    if (willingness == null) return 70;
    Integer totalTime = recipe.getTotalTimeMin();
    Difficulty difficulty = recipe.getDifficulty();
    return switch (willingness) {
      case UNDER_10_MIN -> (totalTime != null && totalTime <= 10) ? 100 : (totalTime != null && totalTime <= 20 ? 60 : 20);
      case SIMPLE -> {
        boolean easyEnough = difficulty == Difficulty.EASY || (totalTime != null && totalTime <= 30);
        yield easyEnough ? 100 : 55;
      }
      case PROPER_COOKING -> 100; // no constraint — willing to do anything
      case TAKEOUT_OK -> (difficulty == Difficulty.EASY || (totalTime != null && totalTime <= 20)) ? 100 : 65;
    };
  }

  private int timeFit(Recipe recipe, Integer timeAvailableMin) {
    if (timeAvailableMin == null || recipe.getTotalTimeMin() == null) return 70;
    int diff = Math.abs(recipe.getTotalTimeMin() - timeAvailableMin);
    if (diff <= 5) return 100;
    if (diff <= 15) return 75;
    if (diff <= 30) return 50;
    return 25;
  }

  /** Recency penalty (spec §15): shown in the last 1/3/7 days loses 40/20/10
   * points off a full-novelty 100. */
  private int novelty(Instant lastShownAt) {
    if (lastShownAt == null) return 100;
    long hoursAgo = ChronoUnit.HOURS.between(lastShownAt, Instant.now());
    if (hoursAgo <= 24) return 60;
    if (hoursAgo <= 72) return 80;
    if (hoursAgo <= 168) return 90;
    return 100;
  }

  /** Placeholder for the fuller "1.한식 2.일식 3.양식..." category spread from
   * spec §15 — with a small seed catalog there usually isn't enough headroom
   * to enforce that without hurting relevance, so for now this just returns
   * the top-scored candidate. Kept as its own method so a real diversity pass
   * (operating over a top-N slate) can replace the body without touching callers. */
  private ScoredRecipe diversify(List<ScoredRecipe> rankedDesc) {
    return rankedDesc.get(0);
  }
}

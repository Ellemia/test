package com.moodfood.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodfood.domain.*;
import com.moodfood.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Records LIKE/DISLIKE/COOKED/SKIPPED (spec §16) and folds it into a simple
 * per-session preference vector (spec §17) via exponential moving average
 * toward (LIKE/COOKED) or away from (DISLIKE) the recipe's own scores.
 * SKIPPED is recorded for analytics but doesn't move the preference vector.
 */
@Service
public class FeedbackService {

  private static final double LIKE_ALPHA = 0.15;
  private static final double DISLIKE_ALPHA = 0.10;

  private final RecommendationFeedbackRepository feedbackRepository;
  private final UserPreferenceRepository preferenceRepository;
  private final RecipeRepository recipeRepository;
  private final ObjectMapper objectMapper;

  public FeedbackService(RecommendationFeedbackRepository feedbackRepository,
                          UserPreferenceRepository preferenceRepository,
                          RecipeRepository recipeRepository,
                          ObjectMapper objectMapper) {
    this.feedbackRepository = feedbackRepository;
    this.preferenceRepository = preferenceRepository;
    this.recipeRepository = recipeRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public RecommendationFeedback record(Long recommendationHistoryId, UUID sessionId, Long recipeId, FeedbackType type) {
    RecommendationFeedback feedback = feedbackRepository.save(
        new RecommendationFeedback(recommendationHistoryId, sessionId, recipeId, type));

    if (type == FeedbackType.LIKE || type == FeedbackType.DISLIKE || type == FeedbackType.COOKED) {
      recipeRepository.findById(recipeId).ifPresent(recipe -> updatePreference(sessionId, recipe, type));
    }
    return feedback;
  }

  private void updatePreference(UUID sessionId, Recipe recipe, FeedbackType type) {
    if (recipe.getScores() == null) return;
    Map<String, Double> current = loadPreference(sessionId);
    Map<String, Integer> recipeDimensions = dimensionsOf(recipe.getScores());

    double alpha = type == FeedbackType.DISLIKE ? -DISLIKE_ALPHA : LIKE_ALPHA;
    for (var entry : recipeDimensions.entrySet()) {
      double existing = current.getOrDefault(entry.getKey(), 50.0);
      double target = entry.getValue();
      double updated = existing + alpha * (target - existing);
      current.put(entry.getKey(), Math.max(0, Math.min(100, updated)));
    }

    savePreference(sessionId, current);
  }

  public Map<String, Double> loadPreference(UUID sessionId) {
    Optional<UserPreference> existing = preferenceRepository.findById(sessionId);
    if (existing.isEmpty()) return new HashMap<>();
    try {
      return objectMapper.readValue(existing.get().getPreference(), new TypeReference<>() {});
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  private void savePreference(UUID sessionId, Map<String, Double> preference) {
    String json;
    try {
      json = objectMapper.writeValueAsString(preference);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize preference vector", e);
    }
    UserPreference entity = preferenceRepository.findById(sessionId)
        .orElseGet(() -> new UserPreference(sessionId, "{}"));
    entity.setPreference(json);
    preferenceRepository.save(entity);
  }

  private Map<String, Integer> dimensionsOf(RecipeScores s) {
    Map<String, Integer> m = new HashMap<>();
    m.put("tasteSpicy", s.getTasteSpicy());
    m.put("tasteSweet", s.getTasteSweet());
    m.put("tasteSavory", s.getTasteSavory());
    m.put("tasteRich", s.getTasteRich());
    m.put("tasteLight", s.getTasteLight());
    m.put("moodComfort", s.getMoodComfort());
    m.put("moodStressRelief", s.getMoodStressRelief());
    m.put("moodEnergy", s.getMoodEnergy());
    m.put("moodNostalgia", s.getMoodNostalgia());
    return m;
  }
}

package com.moodfood.service;

import com.moodfood.domain.Recipe;
import com.moodfood.domain.RecipeScores;
import com.moodfood.domain.Situation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Deterministic, keyword-based tagger — no LLM in the loop (spec §26: "AI는
 * 핵심 추천 알고리즘을 담당하지 않는다"; this project also has no local LLM
 * reachable from this sandbox to begin with). Scores title/category/cuisine/
 * ingredient text against small keyword→score rule tables and averages hits.
 *
 * This is intentionally coarse — good enough to make the recommendation
 * engine produce sane rankings on the seed set, not a substitute for the
 * admin-reviewed tagging + confidence pipeline in spec §21/§27 once a real
 * (rule- or LLM-assisted) tagger is built out.
 */
@Service
public class TaggingService {

  private record Rule(Pattern pattern, String dimension, int score) {}

  private final List<Rule> rules = List.of(
      // Taste
      rule("spicy|매운|매콤|고추|chili|불닭|떡볶이|kimchi|김치", "tasteSpicy", 75),
      rule("sweet|달콤|디저트|dessert|cake|케이크|초콜릿|chocolate|sugar", "tasteSweet", 75),
      rule("soup|stew|찌개|국|tang|broth", "tasteSavory", 65),
      rule("salad|light|가볍|샐러드", "tasteLight", 60),
      rule("fried|튀김|bacon|삼겹살|cream|치즈|cheese|butter", "tasteRich", 65),

      // Mood/emotional association (service-internal, not scientific — §11)
      rule("soup|stew|찌개|국|rice|밥|porridge|죽|noodle|국수", "moodComfort", 70),
      rule("spicy|매운|매콤|고추|불닭|떡볶이", "moodStressRelief", 70),
      rule("dessert|cake|케이크|초콜릿|chocolate|sweet|디저트", "moodHappiness", 70),
      rule("bbq|beef|고기|steak|protein|chicken|닭", "moodEnergy", 55),
      rule("salad|tea|light|샐러드|가볍|수프", "moodCalm", 55),
      rule("traditional|전통|home|집밥|korean|한식|김치", "moodNostalgia", 65),
      rule("salad|fruit|citrus|과일|light|가볍", "moodRefresh", 60),
      rule("steak|dessert|cake|특별|party|파티|축하", "moodReward", 60),

      // Texture / state
      rule("fried|crispy|튀김|바삭", "textureCrispy", 75),
      rule("soup|stew|찌개|국|porridge|죽", "textureSoft", 55),
      rule("noodle|국수|면", "textureChewy", 55),
      rule("steak|bbq|grill|구이", "textureJuicy", 60),
      rule("soup|stew|찌개|국|hot|뜨거운", "textureWarm", 70),
      rule("salad|cold|ice|냉|차가운", "textureCool", 65),
      rule("stew|casserole|찌개|전골|hearty|든든", "textureHearty", 70),
      rule("salad|light|가볍|샐러드", "textureLight", 65)
  );

  private static Rule rule(String pattern, String dimension, int score) {
    return new Rule(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), dimension, score);
  }

  public RecipeScores tag(Recipe recipe) {
    RecipeScores scores = new RecipeScores(recipe);

    String haystack = String.join(" | ",
        nullToEmpty(recipe.getTitle()),
        nullToEmpty(recipe.getCategory()),
        nullToEmpty(recipe.getCuisine()),
        recipe.getIngredients().stream().map(i -> i.getRawText()).reduce("", (a, b) -> a + " " + b),
        recipe.getTags().stream().map(t -> t.getTag()).reduce("", (a, b) -> a + " " + b)
    ).toLowerCase(Locale.ROOT);

    int matched = 0;
    for (Rule r : rules) {
      if (r.pattern().matcher(haystack).find()) {
        matched++;
        applyScore(scores, r.dimension(), r.score());
      }
    }

    // Baseline so untagged dimensions aren't a hard 0 against every context.
    applyFloor(scores, 20);

    double confidence = Math.min(1.0, matched / 6.0);
    scores.setTaggingConfidence(BigDecimal.valueOf(confidence).setScale(3, java.math.RoundingMode.HALF_UP));
    scores.setTaggedBy("RULE");

    inferSituations(recipe);

    return scores;
  }

  private void inferSituations(Recipe recipe) {
    String category = nullToEmpty(recipe.getCategory()).toLowerCase(Locale.ROOT);
    Integer totalTime = recipe.getTotalTimeMin();
    if (category.contains("breakfast") || category.contains("아침")) recipe.addSituation(Situation.BREAKFAST);
    if (totalTime != null && totalTime <= 15) recipe.addSituation(Situation.QUICK_MEAL);
    if (category.contains("dessert") || category.contains("디저트") || category.contains("party") || category.contains("파티")) {
      recipe.addSituation(Situation.PARTY);
    }
    // Every published recipe should work for at least solo dining.
    recipe.addSituation(Situation.SOLO);
    recipe.addSituation(Situation.DINNER);
  }

  private static void applyScore(RecipeScores s, String dimension, int score) {
    switch (dimension) {
      case "tasteSpicy" -> s.setTasteSpicy(Math.max(s.getTasteSpicy(), score));
      case "tasteSweet" -> s.setTasteSweet(Math.max(s.getTasteSweet(), score));
      case "tasteSalty" -> s.setTasteSalty(Math.max(s.getTasteSalty(), score));
      case "tasteSour" -> s.setTasteSour(Math.max(s.getTasteSour(), score));
      case "tasteSavory" -> s.setTasteSavory(Math.max(s.getTasteSavory(), score));
      case "tasteBitter" -> s.setTasteBitter(Math.max(s.getTasteBitter(), score));
      case "tasteRich" -> s.setTasteRich(Math.max(s.getTasteRich(), score));
      case "tasteLight" -> s.setTasteLight(Math.max(s.getTasteLight(), score));
      case "moodComfort" -> s.setMoodComfort(Math.max(s.getMoodComfort(), score));
      case "moodStressRelief" -> s.setMoodStressRelief(Math.max(s.getMoodStressRelief(), score));
      case "moodHappiness" -> s.setMoodHappiness(Math.max(s.getMoodHappiness(), score));
      case "moodEnergy" -> s.setMoodEnergy(Math.max(s.getMoodEnergy(), score));
      case "moodCalm" -> s.setMoodCalm(Math.max(s.getMoodCalm(), score));
      case "moodNostalgia" -> s.setMoodNostalgia(Math.max(s.getMoodNostalgia(), score));
      case "moodRefresh" -> s.setMoodRefresh(Math.max(s.getMoodRefresh(), score));
      case "moodReward" -> s.setMoodReward(Math.max(s.getMoodReward(), score));
      case "textureCrispy" -> s.setTextureCrispy(Math.max(s.getTextureCrispy(), score));
      case "textureSoft" -> s.setTextureSoft(Math.max(s.getTextureSoft(), score));
      case "textureChewy" -> s.setTextureChewy(Math.max(s.getTextureChewy(), score));
      case "textureJuicy" -> s.setTextureJuicy(Math.max(s.getTextureJuicy(), score));
      case "textureWarm" -> s.setTextureWarm(Math.max(s.getTextureWarm(), score));
      case "textureCool" -> s.setTextureCool(Math.max(s.getTextureCool(), score));
      case "textureHearty" -> s.setTextureHearty(Math.max(s.getTextureHearty(), score));
      case "textureLight" -> s.setTextureLight(Math.max(s.getTextureLight(), score));
      default -> throw new IllegalStateException("Unknown score dimension: " + dimension);
    }
  }

  private static void applyFloor(RecipeScores s, int floor) {
    s.setTasteSpicy(Math.max(s.getTasteSpicy(), floor));
    s.setTasteSweet(Math.max(s.getTasteSweet(), floor));
    s.setTasteSalty(Math.max(s.getTasteSalty(), floor));
    s.setTasteSour(Math.max(s.getTasteSour(), floor));
    s.setTasteSavory(Math.max(s.getTasteSavory(), floor));
    s.setTasteBitter(Math.max(s.getTasteBitter(), floor));
    s.setTasteRich(Math.max(s.getTasteRich(), floor));
    s.setTasteLight(Math.max(s.getTasteLight(), floor));
    s.setMoodComfort(Math.max(s.getMoodComfort(), floor));
    s.setMoodStressRelief(Math.max(s.getMoodStressRelief(), floor));
    s.setMoodHappiness(Math.max(s.getMoodHappiness(), floor));
    s.setMoodEnergy(Math.max(s.getMoodEnergy(), floor));
    s.setMoodCalm(Math.max(s.getMoodCalm(), floor));
    s.setMoodNostalgia(Math.max(s.getMoodNostalgia(), floor));
    s.setMoodRefresh(Math.max(s.getMoodRefresh(), floor));
    s.setMoodReward(Math.max(s.getMoodReward(), floor));
    s.setTextureCrispy(Math.max(s.getTextureCrispy(), floor));
    s.setTextureSoft(Math.max(s.getTextureSoft(), floor));
    s.setTextureChewy(Math.max(s.getTextureChewy(), floor));
    s.setTextureJuicy(Math.max(s.getTextureJuicy(), floor));
    s.setTextureWarm(Math.max(s.getTextureWarm(), floor));
    s.setTextureCool(Math.max(s.getTextureCool(), floor));
    s.setTextureHearty(Math.max(s.getTextureHearty(), floor));
    s.setTextureLight(Math.max(s.getTextureLight(), floor));
  }

  private static String nullToEmpty(String s) { return s == null ? "" : s; }
}

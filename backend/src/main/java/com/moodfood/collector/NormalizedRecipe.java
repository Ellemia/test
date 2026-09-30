package com.moodfood.collector;

import com.moodfood.domain.Difficulty;

import java.math.BigDecimal;
import java.util.List;

/** Canonical shape (spec §8) that every source-specific parser converts into,
 * before the recipe ever reaches {@code recipes}. */
public record NormalizedRecipe(
    String title,
    String description,
    Integer servings,
    Integer prepTimeMin,
    Integer cookTimeMin,
    Integer totalTimeMin,
    Difficulty difficulty,
    String category,
    String cuisine,
    String imageUrl,
    List<NormalizedIngredient> ingredients,
    List<String> steps,
    List<String> tags
) {
  public record NormalizedIngredient(String rawText, BigDecimal quantity, String unit) {
  }
}

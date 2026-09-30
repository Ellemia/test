package com.moodfood.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecipeDetailDto(
    Long id,
    String title,
    String description,
    Integer servings,
    Integer prepTimeMin,
    Integer cookTimeMin,
    Integer totalTimeMin,
    String difficulty,
    String category,
    String cuisine,
    String imageUrl,
    List<IngredientDto> ingredients,
    List<String> steps,
    NutritionDto nutrition,
    List<String> tags,
    ProvenanceDto provenance
) {
  public record IngredientDto(String rawText, String canonicalName) {}
  public record NutritionDto(BigDecimal calories, BigDecimal proteinG, BigDecimal fatG,
                              BigDecimal carbsG, BigDecimal sodiumMg, String source) {}
  public record ProvenanceDto(String sourceType, String sourceName, String sourceUrl,
                               String license, boolean attributionRequired) {}
}

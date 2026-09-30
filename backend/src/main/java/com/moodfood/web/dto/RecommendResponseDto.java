package com.moodfood.web.dto;

public record RecommendResponseDto(
    Long recommendationId,
    Long recipeId,
    String title,
    String imageUrl,
    String reason,
    Integer servings,
    Integer totalTimeMin,
    String difficulty,
    Double score,
    ScoreBreakdown breakdown
) {
  public record ScoreBreakdown(int moodFit, int tasteFit, int situationFit, int cookingFit,
                                int timeFit, int budgetFit, int novelty) {}
}

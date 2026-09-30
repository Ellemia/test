package com.moodfood.service;

import com.moodfood.domain.Recipe;

import java.math.BigDecimal;

public record ScoredRecipe(
    Recipe recipe,
    BigDecimal finalScore,
    int moodFit,
    int tasteFit,
    int situationFit,
    int cookingFit,
    int timeFit,
    int budgetFit,
    int novelty,
    String reason
) {
}

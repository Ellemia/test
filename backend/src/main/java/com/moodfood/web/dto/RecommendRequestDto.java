package com.moodfood.web.dto;

import com.moodfood.domain.CookingWillingness;
import com.moodfood.domain.DesiredTaste;
import com.moodfood.domain.Mood;
import com.moodfood.domain.Situation;
import jakarta.validation.constraints.NotNull;

public record RecommendRequestDto(
    @NotNull Mood mood,
    @NotNull DesiredTaste desiredTaste,
    Situation situation,
    CookingWillingness cookingWillingness,
    Integer timeAvailableMin,
    Integer budgetWon
) {
}

package com.moodfood.service;

import com.moodfood.domain.CookingWillingness;
import com.moodfood.domain.DesiredTaste;
import com.moodfood.domain.Mood;
import com.moodfood.domain.Situation;

/** One recommendation request's inputs (spec §12). Only mood + desiredTaste
 * are asked on the first screen; everything else is optional (spec §25). */
public record UserContext(
    Mood mood,
    DesiredTaste desiredTaste,
    Situation situation,          // nullable
    CookingWillingness cookingWillingness, // nullable
    Integer timeAvailableMin,     // nullable
    Integer budgetWon             // nullable — no cost data on recipes yet, kept for future use
) {
}

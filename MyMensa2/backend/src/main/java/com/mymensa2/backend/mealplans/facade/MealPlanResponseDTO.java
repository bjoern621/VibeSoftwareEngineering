package com.mymensa2.backend.mealplans.facade;

import com.mymensa2.backend.meals.facade.MealResponseDTO;

public record MealPlanResponseDTO(
    Integer mealId,
    String date,
    Integer stock,
    MealResponseDTO meal
) {}

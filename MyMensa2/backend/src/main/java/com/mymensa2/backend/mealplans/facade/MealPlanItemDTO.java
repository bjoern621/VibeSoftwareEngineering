package com.mymensa2.backend.mealplans.facade;

import com.mymensa2.backend.meals.facade.MealResponseDTO;

public record MealPlanItemDTO(
    MealResponseDTO meal,
    Integer stock
) {}

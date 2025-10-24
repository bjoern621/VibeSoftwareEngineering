package com.mymensa2.backend.mealplans.facade;

public record MealPlanRequestDTO(
    Integer mealId,
    String date,
    Integer stock
) {}

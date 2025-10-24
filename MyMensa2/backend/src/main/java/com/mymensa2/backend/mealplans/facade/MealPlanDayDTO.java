package com.mymensa2.backend.mealplans.facade;

import java.util.List;

public record MealPlanDayDTO(
    String date,
    List<MealPlanItemDTO> meals
) {}

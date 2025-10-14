package com.mymensa.backend.mealplans.facade;

import java.util.List;

/**
 * DTO für einen Tag im Speiseplan
 * Wird verwendet in der Response von GET /api/meal-plans
 */
public record MealPlanDayDTO(
    String date,                        // Datum im Format YYYY-MM-DD
    List<MealPlanEntryDTO> meals        // Liste der Gerichte mit Stock für diesen Tag
) {}

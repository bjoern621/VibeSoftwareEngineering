package com.mymensa2.backend.mealplans.facade;

import com.mymensa2.backend.meals.facade.MealDTO;

/**
 * DTO für MealPlan-Eintrag (Meal + Stock für einen bestimmten Tag)
 * Wird verwendet in der Response von GET /api/meal-plans
 */
public record MealPlanEntryDTO(
    MealDTO meal,       // Vollständiges Meal-Objekt
    Integer stock       // Verfügbare Portionen (int as per specification)
) {}

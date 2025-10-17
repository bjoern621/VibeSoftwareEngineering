package com.mymensa.backend.mealplans.facade;

import com.mymensa.backend.meals.facade.MealDTO;

/**
 * DTO für die Response bei PUT /api/meal-plans
 */
public record MealPlanResponseDTO(
    Integer mealId,     // ID des Gerichts (int as per specification)
    String date,        // Datum im Format YYYY-MM-DD
    Integer stock,      // Verfügbare Portionen (int as per specification)
    MealDTO meal        // Vollständiges Meal-Objekt
) {}

package com.mymensa.backend.mealplans.facade;

/**
 * DTO für die Erstellung/Aktualisierung eines MealPlan-Eintrags
 * Wird verwendet bei PUT /api/meal-plans
 */
public record MealPlanRequestDTO(
    Integer mealId,     // ID des Gerichts (int as per specification)
    String date,        // Datum im Format YYYY-MM-DD
    Integer stock       // Verfügbare Portionen (int as per specification)
) {}

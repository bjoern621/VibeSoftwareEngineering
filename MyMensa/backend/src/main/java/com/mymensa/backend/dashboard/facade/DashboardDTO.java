package com.mymensa.backend.dashboard.facade;

import java.util.List;

/**
 * DTO für Dashboard-Daten
 * Enthält Gesamteinnahmen, Gesamtausgaben, Gewinn und Meal-Statistiken
 */
public record DashboardDTO(
        Double totalRevenue,              // Gesamteinnahmen
        Double totalExpenses,             // Gesamtausgaben
        Double profit,                    // Gewinn (Revenue - Expenses)
        List<MealStatisticDTO> mealStats  // Kombinierte Statistiken pro Gericht
) {
}

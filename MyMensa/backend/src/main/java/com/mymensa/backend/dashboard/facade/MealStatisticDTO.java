package com.mymensa.backend.dashboard.facade;

/**
 * DTO für Meal-Statistiken im Dashboard
 * Zeigt Gericht-Name, verkaufte Anzahl, Einnahmen, Ausgaben und Gewinn
 */
public record MealStatisticDTO(
        String mealName,        // Name des Gerichts
        Integer quantitySold,   // Anzahl verkaufter Portionen
        Double totalRevenue,    // Gesamteinnahmen (quantitySold * price)
        Double totalExpenses    // Gesamtausgaben (quantitySold * cost)
) {
    /**
     * Berechnet den Gewinn pro Gericht
     */
    public Double getProfit() {
        return totalRevenue - totalExpenses;
    }
}

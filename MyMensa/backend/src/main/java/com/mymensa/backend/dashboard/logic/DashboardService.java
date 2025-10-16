package com.mymensa.backend.dashboard.logic;

import com.mymensa.backend.dashboard.facade.DashboardDTO;
import com.mymensa.backend.dashboard.facade.MealStatisticDTO;
import com.mymensa.backend.meals.dataaccess.Meal;
import com.mymensa.backend.meals.dataaccess.MealRepository;
// TODO: Auskommentierung entfernen, wenn Order-Entity existiert
// import com.mymensa.backend.orders.dataaccess.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service-Klasse für Dashboard-Berechnungen
 * Berechnet Einnahmen, Ausgaben und Statistiken basierend auf Meals
 */
@Service
public class DashboardService {

    @Autowired
    private MealRepository mealRepository;

    // TODO: Auskommentierung entfernen, wenn Order-Entity existiert
    // @Autowired
    // private OrderRepository orderRepository;

    private final Random random = new Random();

    /**
     * Liefert alle Dashboard-Daten
     * - Gesamteinnahmen
     * - Gesamtausgaben
     * - Gewinn
     * - Kombinierte Statistiken pro Gericht
     */
    public DashboardDTO getDashboardData() {
        // Alle Gerichte aus der Datenbank laden
        List<Meal> meals = mealRepository.findAll();

        // WICHTIG: Mock-Verkaufszahlen einmal generieren und speichern
        Map<Integer, Integer> salesByMealId = new HashMap<>();
        for (Meal meal : meals) {
            // TODO: Auskommentierung entfernen, wenn Order-Entity implementiert ist
            /*
            // Echte Implementierung: Anzahl bezahlter Bestellungen aus Datenbank holen
            int quantitySold = orderRepository.countByMealAndIsPaidTrue(meal);
            */

            // MOCK: Zufällige Anzahl verkaufter Portionen (20-100)
            int quantitySold = 20 + random.nextInt(81);
            salesByMealId.put(meal.getId(), quantitySold);
        }

        // Kombinierte Statistiken pro Gericht berechnen
        List<MealStatisticDTO> mealStats = meals.stream()
                .map(meal -> calculateMealStatistic(meal, salesByMealId.get(meal.getId())))
                .collect(Collectors.toList());

        // Gesamteinnahmen berechnen (Summe aller Einnahmen)
        Double totalRevenue = mealStats.stream()
                .mapToDouble(MealStatisticDTO::totalRevenue)
                .sum();

        // Gesamtausgaben berechnen (Summe aller Ausgaben)
        Double totalExpenses = mealStats.stream()
                .mapToDouble(MealStatisticDTO::totalExpenses)
                .sum();

        // Gewinn berechnen
        Double profit = totalRevenue - totalExpenses;

        return new DashboardDTO(
                totalRevenue,
                totalExpenses,
                profit,
                mealStats
        );
    }

    /**
     * Berechnet kombinierte Statistik für ein einzelnes Gericht
     * Enthält Einnahmen UND Ausgaben
     */
    private MealStatisticDTO calculateMealStatistic(Meal meal, int quantitySold) {
        // Gesamteinnahmen = quantitySold * price
        // WICHTIG: Float zu Double konvertieren
        Double totalRevenue = quantitySold * meal.getPrice().doubleValue();

        // Gesamtausgaben = quantitySold * cost
        // WICHTIG: Float zu Double konvertieren
        Double totalExpenses = quantitySold * meal.getCost().doubleValue();

        return new MealStatisticDTO(
                meal.getName(),
                quantitySold,
                totalRevenue,
                totalExpenses
        );
    }
}

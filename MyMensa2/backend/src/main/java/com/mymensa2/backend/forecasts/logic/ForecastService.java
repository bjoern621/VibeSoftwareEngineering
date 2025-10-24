package com.mymensa2.backend.forecasts.logic;

import com.mymensa2.backend.forecasts.facade.*;
import com.mymensa2.backend.inventory.dataaccess.Ingredient;
import com.mymensa2.backend.inventory.dataaccess.IngredientRepository;
import com.mymensa2.backend.mealplans.dataaccess.MealPlan;
import com.mymensa2.backend.mealplans.dataaccess.MealPlanRepository;
import com.mymensa2.backend.orders.dataaccess.Order;
import com.mymensa2.backend.orders.dataaccess.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForecastService {
    
    private final OrderRepository orderRepository;
    private final MealPlanRepository mealPlanRepository;
    private final IngredientRepository ingredientRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ForecastService(OrderRepository orderRepository, MealPlanRepository mealPlanRepository, 
                          IngredientRepository ingredientRepository) {
        this.orderRepository = orderRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.ingredientRepository = ingredientRepository;
    }
    
    // Wareneinsatz-Prognose für Zeitraum
    @Transactional(readOnly = true)
    public DemandForecastResponseDTO getDemandForecast(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
        
        // Analysiere historische Daten (z.B. letzte 30 Tage vor dem Prognosezeitraum)
        LocalDate historicalStart = startDate.minusDays(30);
        LocalDate historicalEnd = startDate.minusDays(1);
        
        List<Order> historicalOrders = orderRepository.findByPickupDateBetween(historicalStart, historicalEnd)
                .stream()
                .filter(Order::getPaid)
                .collect(Collectors.toList());
        
        // Gruppiere nach Gericht
        Map<String, List<Order>> groupedByMeal = historicalOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getMeal().getName()));
        
        // Berechne durchschnittliche tägliche Nachfrage
        long daysBetween = historicalEnd.toEpochDay() - historicalStart.toEpochDay() + 1;
        
        List<MealForecastDTO> mealForecasts = groupedByMeal.entrySet().stream()
                .map(entry -> {
                    String mealName = entry.getKey();
                    int totalOrders = entry.getValue().size();
                    float averageDailyDemand = totalOrders / (float) daysBetween;
                    
                    // Berechne empfohlenen Bestand für den Prognosezeitraum
                    long forecastDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
                    int recommendedStock = Math.round(averageDailyDemand * forecastDays * 1.1f); // 10% Puffer
                    
                    float confidenceLevel = totalOrders > 10 ? 0.85f : 0.65f;
                    
                    return new MealForecastDTO(mealName, averageDailyDemand, recommendedStock, confidenceLevel);
                })
                .collect(Collectors.toList());
        
        // Prognose für Zutaten (vereinfacht)
        List<Ingredient> ingredients = ingredientRepository.findAll();
        List<IngredientForecastDTO> ingredientForecasts = ingredients.stream()
                .map(ingredient -> {
                    // Vereinfachte Schätzung basierend auf historischem Verbrauch
                    float estimatedConsumption = ingredient.getMinStockLevel() * 2;
                    float currentStock = ingredient.getStockQuantity();
                    float recommendedPurchase = Math.max(0, estimatedConsumption - currentStock);
                    
                    return new IngredientForecastDTO(
                            ingredient.getName(),
                            estimatedConsumption,
                            currentStock,
                            recommendedPurchase
                    );
                })
                .collect(Collectors.toList());
        
        return new DemandForecastResponseDTO(
                new ForecastPeriodDTO(startDateStr, endDateStr),
                mealForecasts,
                ingredientForecasts
        );
    }
    
    // Nachhaltigkeit-Bericht
    @Transactional(readOnly = true)
    public SustainabilityReportDTO getSustainabilityReport(Integer month, Integer year) {
        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        
        // Hole alle Speisepläne für den Monat
        List<MealPlan> mealPlans = mealPlanRepository.findByDateBetween(startDate, endDate);
        int totalMealsPrepared = mealPlans.stream()
                .mapToInt(MealPlan::getStock)
                .sum();
        
        // Hole alle bezahlten Bestellungen für den Monat
        List<Order> orders = orderRepository.findByPickupDateBetween(startDate, endDate)
                .stream()
                .filter(Order::getPaid)
                .collect(Collectors.toList());
        int totalMealsSold = orders.size();
        
        int wastedMeals = totalMealsPrepared - totalMealsSold;
        float wastePercentage = totalMealsPrepared > 0 ? (wastedMeals / (float) totalMealsPrepared) * 100 : 0;
        
        // Simuliere Vormonatswert (in Realität würde man dies aus der Datenbank holen)
        float previousMonthWastePercentage = 5.2f;
        float improvement = previousMonthWastePercentage - wastePercentage;
        
        WasteReductionDTO wasteReduction = new WasteReductionDTO(
                totalMealsPrepared,
                totalMealsSold,
                wastedMeals,
                wastePercentage,
                previousMonthWastePercentage,
                improvement
        );
        
        // Berechne Kosteneinsparungen (vereinfacht)
        double savedCosts = improvement > 0 ? improvement * totalMealsPrepared * 3.2 / 100 : 0;
        double potentialSavings = wastePercentage > 0 ? wastePercentage * totalMealsPrepared * 3.2 / 100 : 0;
        
        CostSavingsDTO costSavings = new CostSavingsDTO(savedCosts, potentialSavings);
        
        // Top verschwendete Gerichte (Dummy-Daten für Demo)
        List<TopWastedMealDTO> topWastedMeals = new ArrayList<>();
        
        // Empfehlungen
        List<String> recommendations = new ArrayList<>();
        if (wastePercentage > 5) {
            recommendations.add("Reduktion der Portionsgrößen um 10% erwägen");
            recommendations.add("Genauere Bedarfsplanung durch KI-Prognosen");
        }
        if (improvement > 0) {
            recommendations.add("Aktuelle Strategie beibehalten - Verschwendung erfolgreich reduziert");
        }
        
        String monthName = targetMonth.getMonth().toString() + " " + year;
        
        return new SustainabilityReportDTO(
                monthName,
                wasteReduction,
                costSavings,
                topWastedMeals,
                recommendations
        );
    }
}

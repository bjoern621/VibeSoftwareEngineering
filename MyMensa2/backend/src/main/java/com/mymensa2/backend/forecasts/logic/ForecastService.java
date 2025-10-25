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
        
        // Prognose für Zutaten basierend auf empfohlenen Gerichten
        List<Ingredient> ingredients = ingredientRepository.findAll();
        
        // Erstelle ein Map für schnellen Zugriff auf Zutaten nach Namen
        Map<String, Ingredient> ingredientMap = ingredients.stream()
                .collect(Collectors.toMap(Ingredient::getName, ing -> ing));
        
        // Berechne geschätzten Verbrauch pro Zutat basierend auf Meal-Forecasts
        Map<String, Float> estimatedConsumptionMap = new java.util.HashMap<>();
        
        for (MealForecastDTO mealForecast : mealForecasts) {
            // Hole Zutatenbedarf pro Gericht (vereinfachte Zuordnung basierend auf Meal-Namen)
            Map<String, Float> ingredientsPerMeal = getIngredientRequirementsForMeal(mealForecast.mealName());
            
            // Multipliziere mit empfohlenem Bestand
            for (Map.Entry<String, Float> entry : ingredientsPerMeal.entrySet()) {
                String ingredientName = entry.getKey();
                float quantityPerPortion = entry.getValue();
                float totalQuantity = quantityPerPortion * mealForecast.recommendedStock();
                
                estimatedConsumptionMap.merge(ingredientName, totalQuantity, Float::sum);
            }
        }
        
        // Erstelle Ingredient-Forecasts
        List<IngredientForecastDTO> ingredientForecasts = ingredients.stream()
                .map(ingredient -> {
                    String name = ingredient.getName();
                    // Nutze berechneten Verbrauch, oder 0 falls Zutat nicht verwendet wird
                    float estimatedConsumption = estimatedConsumptionMap.getOrDefault(name, 0.0f);
                    float currentStock = ingredient.getStockQuantity();
                    float recommendedPurchase = Math.max(0, estimatedConsumption - currentStock);
                    
                    return new IngredientForecastDTO(
                            name,
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
    
    /**
     * Gibt die Zutatenmenge pro Portion für ein Gericht zurück
     * 
     * @param mealName Name des Gerichts
     * @return Map mit Zutat -> Menge in kg/Stück
     */
    private Map<String, Float> getIngredientRequirementsForMeal(String mealName) {
        Map<String, Float> requirements = new java.util.HashMap<>();
        
        // Vereinfachte Zuordnung: Zutaten pro Portion
        // In einer echten Anwendung würde dies aus einer Datenbank-Tabelle kommen
        // Zutaten im System: Tomaten, Nudeln, Hackfleisch, Salat, Kartoffeln, Mozzarella, 
        //                     Veganes Patty, Bratwurst, Hähnchen, Lachsfilet, Reis
        switch (mealName) {
            case "Spaghetti Bolognese":
                // Zutaten: Nudeln, Hackfleisch, Tomatensauce, Zwiebeln
                requirements.put("Nudeln", 0.15f);        // 150g Nudeln pro Portion
                requirements.put("Hackfleisch", 0.12f);   // 120g Hackfleisch
                requirements.put("Tomaten", 0.10f);       // 100g Tomaten (Tomatensauce)
                break;
                
            case "Veganer Burger":
                // Zutaten: Veganes Patty, Brötchen, Salat, Tomaten, Zwiebeln
                requirements.put("Veganes Patty", 1.0f);  // 1 Patty pro Burger
                requirements.put("Salat", 0.5f);          // 0.5 Stück Salat
                requirements.put("Tomaten", 0.05f);       // 50g Tomaten
                break;
                
            case "Caesar Salad":
                // Zutaten: Salat, Hähnchen, Parmesan, Croutons, Caesar-Dressing
                requirements.put("Salat", 1.0f);          // 1 Stück Salat (Römersalat)
                requirements.put("Hähnchen", 0.15f);      // 150g Hähnchen
                requirements.put("Tomaten", 0.05f);       // 50g Tomaten als Garnitur
                // Parmesan, Croutons, Dressing nicht im Lager
                break;
                
            case "Currywurst mit Pommes":
                // Zutaten: Bratwurst, Currysauce, Pommes Frites
                requirements.put("Bratwurst", 1.5f);      // 1.5 Bratwurst pro Portion
                requirements.put("Kartoffeln", 0.30f);    // 300g Kartoffeln für Pommes
                requirements.put("Tomaten", 0.05f);       // 50g Tomaten für Currysauce
                break;
                
            case "Gemüse-Lasagne":
                // Zutaten: Lasagneplatten, Zucchini, Aubergine, Tomatensauce, Mozzarella
                requirements.put("Nudeln", 0.10f);        // 100g Lasagneplatten (Nudelteig)
                requirements.put("Tomaten", 0.15f);       // 150g Tomaten (Tomatensauce)
                requirements.put("Mozzarella", 0.08f);    // 80g Mozzarella
                // Zucchini, Aubergine nicht im Lager
                break;
                
            case "Gegrillter Lachs mit Reis":
                // Zutaten: Lachsfilet, Jasminreis, Brokkoli, Karotten, Zitrone
                requirements.put("Lachsfilet", 0.20f);    // 200g Lachs pro Portion
                requirements.put("Reis", 0.12f);          // 120g Reis (roh)
                requirements.put("Tomaten", 0.08f);       // 80g Tomaten als Beilage
                // Brokkoli, Karotten, Zitrone nicht im Lager
                break;
                
            case "Hähnchen Shawarma":
                // Zutaten: Halal Hähnchen, Fladenbrot, Hummus, Tahini, Tomaten, Gurken
                requirements.put("Hähnchen", 0.18f);      // 180g Hähnchen (Halal)
                requirements.put("Tomaten", 0.08f);       // 80g Tomaten
                requirements.put("Salat", 0.3f);          // 0.3 Stück Salat als Beilage
                // Fladenbrot, Hummus, Tahini, Gurken nicht im Lager
                break;
                
            case "Quinoa Bowl":
                // Zutaten: Quinoa, Süßkartoffel, Kichererbsen, Avocado, Spinat, Tahini-Dressing
                requirements.put("Kartoffeln", 0.15f);    // 150g Süßkartoffel (ähnlich wie Kartoffeln)
                requirements.put("Tomaten", 0.07f);       // 70g Tomaten
                requirements.put("Salat", 0.3f);          // 0.3 Stück Salat (Spinat)
                // Quinoa, Kichererbsen, Avocado, Tahini-Dressing nicht im Lager
                break;
                
            default:
                // Fallback: Keine spezifischen Zutaten bekannt
                break;
        }
        
        return requirements;
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

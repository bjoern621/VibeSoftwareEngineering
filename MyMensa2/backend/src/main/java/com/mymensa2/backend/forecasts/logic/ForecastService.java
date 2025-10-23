package com.mymensa2.backend.forecasts.logic;

import com.mymensa2.backend.forecasts.facade.*;
import com.mymensa2.backend.inventory.dataaccess.Ingredient;
import com.mymensa2.backend.inventory.dataaccess.IngredientRepository;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class ForecastService {
    
    private final IngredientRepository ingredientRepository;
    
    public ForecastService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }
    
    // Wareneinsatz-Prognose (vereinfacht mit simulierten Daten)
    public ForecastResponseDTO getDemandForecast(String startDate, String endDate) {
        ForecastPeriodDTO period = new ForecastPeriodDTO(startDate, endDate);
        
        // Simulierte Meal-Prognosen
        List<MealForecastDTO> mealForecasts = Arrays.asList(
            new MealForecastDTO("Spaghetti Bolognese", 18.5f, 95, 0.85f),
            new MealForecastDTO("Veganer Burger", 15.2f, 80, 0.78f),
            new MealForecastDTO("Hähnchen-Curry", 12.8f, 65, 0.82f),
            new MealForecastDTO("Caesar Salad", 8.5f, 45, 0.75f)
        );
        
        // Ingredient-Prognosen basierend auf echten Lagerbeständen
        List<IngredientForecastDTO> ingredientForecasts = new ArrayList<>();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        
        for (Ingredient ingredient : ingredients) {
            // Vereinfachte Schätzung: 30% des aktuellen Bestands als wöchentlicher Verbrauch
            Float estimatedConsumption = ingredient.getStockQuantity() * 0.3f;
            Float recommendedPurchase = 0.0f;
            
            if (estimatedConsumption > ingredient.getStockQuantity()) {
                recommendedPurchase = estimatedConsumption - ingredient.getStockQuantity() + ingredient.getMinStockLevel();
            }
            
            ingredientForecasts.add(new IngredientForecastDTO(
                ingredient.getName(),
                estimatedConsumption,
                ingredient.getStockQuantity(),
                recommendedPurchase
            ));
        }
        
        return new ForecastResponseDTO(period, mealForecasts, ingredientForecasts);
    }
    
    // Nachhaltigkeits-Bericht (vereinfacht mit simulierten Daten)
    public SustainabilityReportDTO getSustainabilityReport(Integer month, Integer year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.GERMAN);
        String period = monthName + " " + year;
        
        // Simulierte Waste-Reduction-Daten
        WasteReductionDTO wasteReduction = new WasteReductionDTO(
            2450,  // totalMealsPrepared
            2380,  // totalMealsSold
            70,    // wastedMeals
            2.86f, // wastePercentage
            5.20f, // previousMonthWastePercentage
            2.34f  // improvement
        );
        
        // Cost-Savings
        CostSavingsDTO costSavings = new CostSavingsDTO(
            224.0,  // savedCosts
            156.0   // potentialSavings
        );
        
        // Top verschwendete Gerichte
        List<TopWastedMealDTO> topWastedMeals = Arrays.asList(
            new TopWastedMealDTO("Caesar Salad", 25, 75.0),
            new TopWastedMealDTO("Tomatensuppe", 18, 37.8),
            new TopWastedMealDTO("Glutenfreie Pizza", 15, 78.0)
        );
        
        // Empfehlungen
        List<String> recommendations = Arrays.asList(
            "Reduktion der Caesar Salad Portionen um 10%",
            "Erhöhung der Veganer Burger Produktion um 5%",
            "Implementierung eines Vorbestellungssystems für glutenfreie Optionen"
        );
        
        return new SustainabilityReportDTO(
            period,
            wasteReduction,
            costSavings,
            topWastedMeals,
            recommendations
        );
    }
}

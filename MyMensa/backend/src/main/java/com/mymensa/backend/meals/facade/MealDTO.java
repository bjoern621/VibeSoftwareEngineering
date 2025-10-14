package com.mymensa.backend.meals.facade;

import java.util.List;

/**
 * DTO for Meal data transfer using Java Record
 * Records are immutable and automatically provide:
 * - Getters (e.g., id(), name(), price())
 * - Constructor
 * - equals(), hashCode(), toString()
 */
public record MealDTO(
    Integer id,          // int as per specification
    String name,
    String description,
    Float price,         // float as per specification
    Float cost,          // float as per specification
    Integer stock,       // int as per specification
    String ingredients,
    NutritionalInfoDTO nutritionalInfo,
    List<String> categories,
    List<String> allergens
) {
    /**
     * Nested Record for Nutritional Information
     */
    public record NutritionalInfoDTO(
        Integer calories,    // int as per specification
        Float protein,       // float as per specification
        Float carbs,         // float as per specification
        Float fat            // float as per specification
    ) {}
}

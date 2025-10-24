package com.mymensa2.backend.meals.facade;

import com.mymensa2.backend.meals.dataaccess.NutritionalInfo;

import java.util.List;

public record MealRequestDTO(
    String name,
    String description,
    Float price,
    Float cost,
    String ingredients,
    NutritionalInfo nutritionalInfo,
    List<String> categories,
    List<String> allergens
) {}

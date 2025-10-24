package com.mymensa2.backend.forecasts.facade;

public record IngredientForecastDTO(
    String ingredientName,
    Float estimatedConsumption,
    Float currentStock,
    Float recommendedPurchase
) {}

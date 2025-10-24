package com.mymensa2.backend.forecasts.facade;

public record MealForecastDTO(
    String mealName,
    Float averageDailyDemand,
    Integer recommendedStock,
    Float confidenceLevel
) {}

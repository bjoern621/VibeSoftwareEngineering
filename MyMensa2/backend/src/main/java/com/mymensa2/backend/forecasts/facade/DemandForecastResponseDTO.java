package com.mymensa2.backend.forecasts.facade;

import java.util.List;

public record DemandForecastResponseDTO(
    ForecastPeriodDTO forecastPeriod,
    List<MealForecastDTO> mealForecasts,
    List<IngredientForecastDTO> ingredientForecasts
) {}

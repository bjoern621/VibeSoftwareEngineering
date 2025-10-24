package com.mymensa2.backend.forecasts.facade;

public record WasteReductionDTO(
    Integer totalMealsPrepared,
    Integer totalMealsSold,
    Integer wastedMeals,
    Float wastePercentage,
    Float previousMonthWastePercentage,
    Float improvement
) {}

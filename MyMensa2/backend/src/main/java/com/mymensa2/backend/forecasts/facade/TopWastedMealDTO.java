package com.mymensa2.backend.forecasts.facade;

public record TopWastedMealDTO(
    String mealName,
    Integer wastedPortions,
    Double costOfWaste
) {}

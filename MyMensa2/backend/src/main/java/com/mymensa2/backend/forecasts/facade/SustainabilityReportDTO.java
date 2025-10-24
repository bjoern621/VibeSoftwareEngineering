package com.mymensa2.backend.forecasts.facade;

import java.util.List;

public record SustainabilityReportDTO(
    String period,
    WasteReductionDTO wasteReduction,
    CostSavingsDTO costSavings,
    List<TopWastedMealDTO> topWastedMeals,
    List<String> recommendations
) {}

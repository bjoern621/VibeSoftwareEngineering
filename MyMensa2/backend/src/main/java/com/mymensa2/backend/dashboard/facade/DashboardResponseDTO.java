package com.mymensa2.backend.dashboard.facade;

import java.util.List;

public record DashboardResponseDTO(
    Double totalRevenue,
    Double totalExpenses,
    Double profit,
    List<MealStatDTO> mealStats
) {}

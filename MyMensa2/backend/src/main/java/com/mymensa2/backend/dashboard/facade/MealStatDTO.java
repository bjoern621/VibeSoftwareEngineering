package com.mymensa2.backend.dashboard.facade;

public record MealStatDTO(
    String mealName,
    Integer quantitySold,
    Double totalRevenue,
    Double totalExpenses
) {}

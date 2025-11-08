package com.travelreimburse.application.dto;

import java.math.BigDecimal;

/**
 * DTO für Budget-Übersicht (Presentation Layer)
 * Wird vom ReportingService an Controller zurückgegeben
 */
public record BudgetOverviewDTO(
    BigDecimal allocatedBudget,
    BigDecimal usedBudget,
    BigDecimal remainingBudget,
    String currency,
    boolean budgetExceeded,
    BigDecimal overrunAmount,
    double utilizationPercentage,
    boolean criticalUtilization
) {
}

package com.travelreimburse.application.dto;

import java.math.BigDecimal;

/**
 * DTO für Kostenstellen-Report (Presentation Layer)
 * Wird vom ReportingService an Controller zurückgegeben
 */
public record CostCenterReportDTO(
    String costCenterCode,
    String costCenterName,
    long travelRequestCount,
    BigDecimal totalCost,
    String currency,
    BigDecimal averageCostPerTrip,
    long employeeCount,
    double averageTravelsPerEmployee,
    boolean active
) {
}

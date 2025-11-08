package com.travelreimburse.application.dto;

import java.math.BigDecimal;

/**
 * DTO für Reisestatistiken-Report (Presentation Layer)
 * Wird vom ReportingService an Controller zurückgegeben
 */
public record TravelStatisticsResponseDTO(
    String periodStart,
    String periodEnd,
    long totalRequests,
    long approvedRequests,
    long rejectedRequests,
    long pendingRequests,
    BigDecimal totalEstimatedCost,
    String currency,
    BigDecimal averageCostPerTrip,
    long totalTravelDays,
    double approvalRate,
    double rejectionRate
) {
}

package com.travelreimburse.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO für die Response eines Reiseabschnitts
 */
public record TravelLegResponseDTO(
    Long id,
    String departureLocation,
    String arrivalLocation,
    String transportationType,
    BigDecimal costAmount,
    String currency,
    String description,
    LocalDateTime departureDateTime,
    LocalDateTime arrivalDateTime,
    Double distanceKm,
    LocalDateTime createdAt
) {
}

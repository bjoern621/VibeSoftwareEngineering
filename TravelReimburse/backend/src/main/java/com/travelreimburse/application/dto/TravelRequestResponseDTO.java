package com.travelreimburse.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für die Rückgabe eines Reiseantrags
 * Enthält alle relevanten Informationen für die Präsentation
 */
public record TravelRequestResponseDTO(
    Long id,
    Long employeeId,
    String destination,
    String purpose,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal estimatedAmount,
    String currency,
    String status,
    LocalDateTime createdAt,
    LocalDateTime submittedAt,
    Long approverId,
    LocalDateTime approvedAt,
    LocalDateTime rejectedAt,
    String rejectionReason,
    List<TravelLegResponseDTO> travelLegs
) {
}

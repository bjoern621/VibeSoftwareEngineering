package com.travelreimburse.application.dto;

import java.time.LocalDateTime;

/**
 * DTO für die Response eines Reiseziels
 */
public record TravelDestinationResponseDTO(
    Long id,
    String countryCode,
    String countryName,
    boolean isEuropeanUnion,
    VisaRequirementDTO visaRequirement,
    VaccinationRequirementDTO vaccinationRequirement,
    String generalTravelAdvice,
    boolean requiresPreparation,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

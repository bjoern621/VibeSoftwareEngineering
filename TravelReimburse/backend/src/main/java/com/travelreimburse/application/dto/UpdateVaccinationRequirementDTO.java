package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO für die Aktualisierung von Impfanforderungen
 */
public record UpdateVaccinationRequirementDTO(
    @NotNull(message = "Impfpflicht muss angegeben werden")
    Boolean required,
    
    String requiredVaccinations,
    
    String recommendedVaccinations,
    
    String notes
) {
}

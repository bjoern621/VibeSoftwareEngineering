package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO für Impfanforderungen
 */
public record VaccinationRequirementDTO(
    @NotNull(message = "Impfpflicht muss angegeben werden")
    Boolean required,
    
    String requiredVaccinations,  // Kommasepariert, z.B. "Gelbfieber, Hepatitis A"
    
    String recommendedVaccinations,  // Kommasepariert
    
    String notes  // Optional
) {
}

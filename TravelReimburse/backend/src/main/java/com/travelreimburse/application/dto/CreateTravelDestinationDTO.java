package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO für die Erstellung eines neuen Reiseziels
 * 
 * DDD: DTOs nur im Application/Presentation Layer, nicht in Domain
 */
public record CreateTravelDestinationDTO(
    @NotBlank(message = "Ländercode darf nicht leer sein")
    String countryCode,
    
    @NotBlank(message = "Ländername darf nicht leer sein")
    String countryName,
    
    @NotNull(message = "Visa-Anforderungen müssen angegeben werden")
    VisaRequirementDTO visaRequirement,
    
    @NotNull(message = "Impfanforderungen müssen angegeben werden")
    VaccinationRequirementDTO vaccinationRequirement,
    
    String generalTravelAdvice
) {
}

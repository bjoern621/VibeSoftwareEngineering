package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO für die Aktualisierung von Visa-Anforderungen
 */
public record UpdateVisaRequirementDTO(
    @NotNull(message = "Visa-Pflicht muss angegeben werden")
    Boolean required,
    
    String type,
    
    Integer processingDays,
    
    String notes
) {
}

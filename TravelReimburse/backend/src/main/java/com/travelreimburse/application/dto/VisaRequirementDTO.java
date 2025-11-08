package com.travelreimburse.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO für Visa-Anforderungen
 */
public record VisaRequirementDTO(
    @NotNull(message = "Visa-Pflicht muss angegeben werden")
    Boolean required,
    
    String type,  // Optional: nur wenn required=true
    
    @Min(value = 0, message = "Bearbeitungstage dürfen nicht negativ sein")
    Integer processingDays,  // Optional
    
    String notes  // Optional
) {
}

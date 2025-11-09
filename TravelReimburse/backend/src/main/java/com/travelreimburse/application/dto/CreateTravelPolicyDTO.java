package com.travelreimburse.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * DTO für das Erstellen einer neuen TravelPolicy
 */
public record CreateTravelPolicyDTO(
    @NotBlank(message = "Policy-Name darf nicht leer sein")
    String name,
    
    String description,
    
    String departmentCode,  // Optional: null = gilt für alle Departments
    
    String location,  // Optional: null = gilt für alle Standorte
    
    @NotNull(message = "Auto-Approval-Flag muss angegeben werden")
    Boolean autoApprovalEnabled,
    
    /**
     * Map von Kategorie zu Höchstbetrag (in EUR)
     * Key: ACCOMMODATION, MEALS, TRANSPORTATION, etc.
     * Value: Betrag als String (z.B. "150.00")
     */
    Map<String, String> categoryLimits
) {
}

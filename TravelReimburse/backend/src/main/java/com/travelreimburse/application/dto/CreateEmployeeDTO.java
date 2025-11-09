package com.travelreimburse.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO für das Erstellen eines neuen Employees
 */
public record CreateEmployeeDTO(
    @NotBlank(message = "Vorname darf nicht leer sein")
    String firstName,
    
    @NotBlank(message = "Nachname darf nicht leer sein")
    String lastName,
    
    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "E-Mail muss gültig sein")
    String email,
    
    @NotNull(message = "Rolle darf nicht null sein")
    String role, // String statt Enum für einfachere API-Nutzung
    
    Long managerId, // Optional
    
    String departmentCode, // Optional - z.B. "IT-001", "SALES-001"
    
    String location // Optional - z.B. "Munich", "Berlin"
) {
}

package com.travelreimburse.application.dto;

import jakarta.validation.constraints.Email;

/**
 * DTO für das Aktualisieren eines Employees
 * Alle Felder sind optional
 */
public record UpdateEmployeeDTO(
    String firstName,
    String lastName,
    
    @Email(message = "E-Mail muss gültig sein")
    String email,
    
    String role,
    Long managerId,
    String departmentCode,
    String location
) {
}

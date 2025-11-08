package com.travelreimburse.application.dto;

import java.time.LocalDateTime;

/**
 * DTO für Employee-Responses
 * Wird von der API zurückgegeben
 */
public record EmployeeResponseDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String role,
    Long managerId,
    Boolean active,
    LocalDateTime createdAt
) {
}

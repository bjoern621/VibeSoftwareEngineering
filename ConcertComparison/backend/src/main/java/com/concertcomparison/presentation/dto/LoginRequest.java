package com.concertcomparison.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO für User-Login.
 */
public record LoginRequest(
        
        @Email(message = "Email muss gültiges Format haben")
        @NotBlank(message = "Email darf nicht leer sein")
        String email,
        
        @NotBlank(message = "Password darf nicht leer sein")
        String password
) {
}

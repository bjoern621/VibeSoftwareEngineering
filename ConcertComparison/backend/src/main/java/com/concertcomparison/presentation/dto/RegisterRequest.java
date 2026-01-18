package com.concertcomparison.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO für User-Registrierung.
 */
public record RegisterRequest(
        
        @Email(message = "Email muss gültiges Format haben")
        @NotBlank(message = "Email darf nicht leer sein")
        String email,
        
        @NotBlank(message = "Password darf nicht leer sein")
        @Size(min = 8, max = 100, message = "Password muss zwischen 8 und 100 Zeichen lang sein")
        String password,
        
        @NotBlank(message = "Vorname darf nicht leer sein")
        @Size(min = 1, max = 100, message = "Vorname muss zwischen 1 und 100 Zeichen lang sein")
        String firstName,
        
        @NotBlank(message = "Nachname darf nicht leer sein")
        @Size(min = 1, max = 100, message = "Nachname muss zwischen 1 und 100 Zeichen lang sein")
        String lastName
) {
}

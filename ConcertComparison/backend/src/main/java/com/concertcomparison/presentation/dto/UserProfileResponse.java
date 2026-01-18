package com.concertcomparison.presentation.dto;

/**
 * Response DTO für User-Profil.
 */
public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        Boolean enabled
) {
}

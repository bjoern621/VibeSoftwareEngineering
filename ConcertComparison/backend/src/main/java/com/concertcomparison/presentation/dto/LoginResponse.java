package com.concertcomparison.presentation.dto;

/**
 * Response DTO für erfolgreiches Login.
 */
public record LoginResponse(
        String token,
        String type,
        String email,
        String role
) {
    /**
     * Factory Method für LoginResponse mit Bearer Token.
     */
    public static LoginResponse of(String token, String email, String role) {
        return new LoginResponse(token, "Bearer", email, role);
    }
}

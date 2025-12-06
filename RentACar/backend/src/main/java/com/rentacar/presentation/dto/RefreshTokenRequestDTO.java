package com.rentacar.presentation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO für Refresh-Token-Request.
 */
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh Token darf nicht leer sein")
    private String refreshToken;

    // Constructors
    public RefreshTokenRequestDTO() {
    }

    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}


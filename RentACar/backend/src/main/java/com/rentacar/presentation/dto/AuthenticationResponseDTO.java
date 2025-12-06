package com.rentacar.presentation.dto;

/**
 * DTO für Authentication Response mit JWT-Token und Refresh-Token.
 */
public class AuthenticationResponseDTO {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long customerId;
    private String email;

    // Constructors
    public AuthenticationResponseDTO() {
    }

    public AuthenticationResponseDTO(String token, String refreshToken, Long customerId, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.customerId = customerId;
        this.email = email;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

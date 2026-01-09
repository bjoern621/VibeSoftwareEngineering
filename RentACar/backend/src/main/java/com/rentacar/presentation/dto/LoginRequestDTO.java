package com.rentacar.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO für Login-Anfrage.
 */
public class LoginRequestDTO {

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "E-Mail-Format ist ungültig")
    private String email;

    @NotBlank(message = "Passwort darf nicht leer sein")
    private String password;

    // Constructors
    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

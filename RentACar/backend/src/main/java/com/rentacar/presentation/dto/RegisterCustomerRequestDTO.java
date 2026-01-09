package com.rentacar.presentation.dto;

import com.rentacar.infrastructure.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO für Kunden-Registrierung.
 */
public class RegisterCustomerRequestDTO {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;

    @NotBlank(message = "Vorname darf nicht leer sein")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "Vorname muss zwischen 2 und 100 Zeichen lang sein")
    private String firstName;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "Nachname muss zwischen 2 und 100 Zeichen lang sein")
    private String lastName;

    @NotBlank(message = "Straße darf nicht leer sein")
    private String street;

    @NotBlank(message = "Postleitzahl darf nicht leer sein")
    private String postalCode;

    @NotBlank(message = "Stadt darf nicht leer sein")
    private String city;

    @NotBlank(message = "Führerscheinnummer darf nicht leer sein")
    private String driverLicenseNumber;

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "E-Mail-Format ist ungültig")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @ValidPassword
    private String password;

    /**
     * Default Constructor für Jackson Deserialisierung und Bean Validation.
     * Felder werden via Setter befüllt.
     */
    public RegisterCustomerRequestDTO() {
        // No-args constructor für Jackson/JPA
    }

    // Getters and Setters
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDriverLicenseNumber() {
        return driverLicenseNumber;
    }

    public void setDriverLicenseNumber(String driverLicenseNumber) {
        this.driverLicenseNumber = driverLicenseNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

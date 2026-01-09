package com.rentacar.presentation.controller;

import com.rentacar.application.service.CustomerApplicationService;
import com.rentacar.presentation.dto.RegisterCustomerRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration-Tests für Passwort-Validierung in {@link CustomerController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CustomerController - Passwort-Validierung Tests")
class CustomerControllerPasswordValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerApplicationService customerApplicationService;

    @Test
    @DisplayName("Registrierung mit gültigem Passwort sollte erfolgreich sein")
    void registerCustomer_WithValidPassword_ShouldSucceed() throws Exception {
        // Given
        String validRequestJson = """
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "street": "Musterstraße 1",
                    "postalCode": "12345",
                    "city": "Musterstadt",
                    "driverLicenseNumber": "B1234567890",
                    "email": "max.mustermann@example.com",
                    "phoneNumber": "0123456789",
                    "password": "Passw0rd"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short",           // zu kurz, keine Großbuchstaben, keine Zahlen
            "Short1",          // zu kurz
            "password",        // keine Großbuchstaben, keine Zahlen
            "PASSWORD",        // keine Kleinbuchstaben, keine Zahlen
            "12345678",        // keine Buchstaben
            "Passwordd",       // keine Zahl
            "passw0rd",        // keine Großbuchstaben
            "PASSW0RD"         // keine Kleinbuchstaben
    })
    @DisplayName("Registrierung mit ungültigem Passwort sollte HTTP 400 zurückgeben")
    void registerCustomer_WithInvalidPassword_ShouldReturnBadRequest(String invalidPassword) throws Exception {
        // Given
        String invalidRequestJson = String.format("""
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "street": "Musterstraße 1",
                    "postalCode": "12345",
                    "city": "Musterstadt",
                    "driverLicenseNumber": "B1234567890",
                    "email": "max.mustermann@example.com",
                    "phoneNumber": "0123456789",
                    "password": "%s"
                }
                """, invalidPassword);

        // When & Then
        mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());

        // Verify service was never called
        verify(customerApplicationService, never()).registerCustomer(any(RegisterCustomerRequestDTO.class));
    }

    @Test
    @DisplayName("Registrierung ohne Passwort sollte HTTP 400 zurückgeben")
    void registerCustomer_WithoutPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        String requestWithoutPassword = """
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "street": "Musterstraße 1",
                    "postalCode": "12345",
                    "city": "Musterstadt",
                    "driverLicenseNumber": "B1234567890",
                    "email": "max.mustermann@example.com",
                    "phoneNumber": "0123456789"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithoutPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());

        verify(customerApplicationService, never()).registerCustomer(any(RegisterCustomerRequestDTO.class));
    }

    @Test
    @DisplayName("Registrierung mit leerem Passwort sollte HTTP 400 zurückgeben")
    void registerCustomer_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        String requestWithEmptyPassword = """
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "street": "Musterstraße 1",
                    "postalCode": "12345",
                    "city": "Musterstadt",
                    "driverLicenseNumber": "B1234567890",
                    "email": "max.mustermann@example.com",
                    "phoneNumber": "0123456789",
                    "password": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithEmptyPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());

        verify(customerApplicationService, never()).registerCustomer(any(RegisterCustomerRequestDTO.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "MyP@ssw0rd!",
            "Secur3#Pass",
            "T3st$Pass!",
            "C0mpl3x&Pwd"
    })
    @DisplayName("Registrierung mit komplexem Passwort inkl. Sonderzeichen sollte erfolgreich sein")
    void registerCustomer_WithComplexPassword_ShouldSucceed(String complexPassword) throws Exception {
        // Given
        String validRequestJson = String.format("""
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "street": "Musterstraße 1",
                    "postalCode": "12345",
                    "city": "Musterstadt",
                    "driverLicenseNumber": "B1234567890",
                    "email": "max.mustermann@example.com",
                    "phoneNumber": "0123456789",
                    "password": "%s"
                }
                """, complexPassword);

        // When & Then
        mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson))
                .andExpect(status().isCreated());
    }
}


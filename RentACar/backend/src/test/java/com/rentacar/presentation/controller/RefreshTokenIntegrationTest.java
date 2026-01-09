package com.rentacar.presentation.controller;

import com.rentacar.application.service.CustomerApplicationService;
import com.rentacar.presentation.dto.AuthenticationResponseDTO;
import com.rentacar.presentation.dto.LoginRequestDTO;
import com.rentacar.presentation.dto.RefreshTokenRequestDTO;
import com.rentacar.presentation.dto.RegisterCustomerRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test für Refresh Token Mechanismus.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerApplicationService customerApplicationService;

    private RegisterCustomerRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        // Use a UUID to ensure unique email for each test
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = "max.refresh." + uniqueSuffix + "@test.de";

        registerRequest = new RegisterCustomerRequestDTO();
        registerRequest.setFirstName("Max");
        registerRequest.setLastName("Mustermann");
        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword("Test1234!");
        registerRequest.setPhoneNumber("+49123456789");
        registerRequest.setStreet("Teststraße 1");
        registerRequest.setPostalCode("12345");
        registerRequest.setCity("Teststadt");
        // Driver license must be exactly 11 characters
        registerRequest.setDriverLicenseNumber("DE" + uniqueSuffix + "X");
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully() throws Exception {
        // 1. Register a new customer
        MvcResult registerResult = mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthenticationResponseDTO authResponse = objectMapper.readValue(registerResponse, AuthenticationResponseDTO.class);

        String initialAccessToken = authResponse.getToken();
        String refreshToken = authResponse.getRefreshToken();

        assertThat(initialAccessToken).isNotNull().isNotEmpty();
        assertThat(refreshToken).isNotNull().isNotEmpty();

        // Wait 1 second to ensure new token has different timestamp
        Thread.sleep(1000);

        // 2. Use refresh token to get a new access token
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String refreshResponse = refreshResult.getResponse().getContentAsString();
        AuthenticationResponseDTO newAuthResponse = objectMapper.readValue(refreshResponse, AuthenticationResponseDTO.class);

        String newAccessToken = newAuthResponse.getToken();
        String newRefreshToken = newAuthResponse.getRefreshToken();

        // Verify new tokens are different from old tokens
        assertThat(newAccessToken).isNotNull().isNotEmpty();
        assertThat(newAccessToken).isNotEqualTo(initialAccessToken);

        assertThat(newRefreshToken).isNotNull().isNotEmpty();
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);
    }

    @Test
    void shouldRejectInvalidRefreshToken() throws Exception {
        RefreshTokenRequestDTO invalidRequest = new RefreshTokenRequestDTO("invalid-token-12345");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() throws Exception {
        // 1. Register and login
        MvcResult registerResult = mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthenticationResponseDTO authResponse = objectMapper.readValue(registerResponse, AuthenticationResponseDTO.class);

        String accessToken = authResponse.getToken();
        String refreshToken = authResponse.getRefreshToken();

        // 2. Logout
        mockMvc.perform(post("/api/kunden/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 3. Try to use the refresh token - should fail
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldOnlyAllowOneTimeRefreshTokenUse() throws Exception {
        // 1. Register
        MvcResult registerResult = mockMvc.perform(post("/api/kunden/registrierung")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthenticationResponseDTO authResponse = objectMapper.readValue(registerResponse, AuthenticationResponseDTO.class);

        String refreshToken = authResponse.getRefreshToken();

        // 2. Use refresh token once - should succeed
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk());

        // 3. Try to use same refresh token again - should fail (token rotation)
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }
}


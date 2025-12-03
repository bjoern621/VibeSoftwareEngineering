package com.rentacar.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentacar.application.service.CustomerApplicationService;
import com.rentacar.domain.service.TokenBlacklistService;
import com.rentacar.infrastructure.security.LoginRateLimiterService;
import com.rentacar.presentation.exception.GlobalExceptionHandler;
import com.rentacar.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


/**
 * Integration-Tests für Login Rate Limiting.
 */
@WebMvcTest(CustomerController.class)
@ContextConfiguration(classes = {CustomerController.class, GlobalExceptionHandler.class, TestSecurityConfig.class})
class CustomerControllerRateLimitingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerApplicationService customerApplicationService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    private LoginRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        // Neuen RateLimiterService für jeden Test erstellen
        rateLimiterService = new LoginRateLimiterService();
    }

    @Test
    void testLoginRateLimiting_BlocksAfter5Attempts()  {
        String testEmail = "ratelimit@example.com";

        // Simuliere 5 fehlgeschlagene Login-Versuche
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(testEmail);
        }

        // 6. Versuch sollte mit HTTP 429 blockiert werden
        // In der realen Anwendung würde der Service die Exception werfen
        // Hier testen wir nur die Service-Logik, da MockMvc den Service mockt

        // Direkter Test des RateLimiterService
        org.junit.jupiter.api.Assertions.assertThrows(
            com.rentacar.domain.exception.TooManyLoginAttemptsException.class,
            () -> rateLimiterService.checkLoginAttempt(testEmail)
        );
    }

    @Test
    void testLoginRateLimiting_ResetsAfterSuccessfulLogin() {
        String testEmail = "success@example.com";

        // 3 Versuche verbrauchen
        for (int i = 0; i < 3; i++) {
            rateLimiterService.checkLoginAttempt(testEmail);
        }

        // Verbleibende Versuche prüfen
        long remaining = rateLimiterService.getRemainingAttempts(testEmail);
        org.junit.jupiter.api.Assertions.assertEquals(2, remaining);

        // Erfolgreicher Login resettet Counter
        rateLimiterService.resetLoginAttempts(testEmail);

        // Jetzt sollten wieder 5 Versuche verfügbar sein
        remaining = rateLimiterService.getRemainingAttempts(testEmail);
        org.junit.jupiter.api.Assertions.assertEquals(5, remaining);
    }

    @Test
    void testLoginRateLimiting_RetryAfterHeader()  {
        String testEmail = "retry@example.com";

        // Alle 5 Versuche verbrauchen
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(testEmail);
        }

        // Exception sollte Retry-After-Sekunden enthalten
        com.rentacar.domain.exception.TooManyLoginAttemptsException exception =
            org.junit.jupiter.api.Assertions.assertThrows(
                com.rentacar.domain.exception.TooManyLoginAttemptsException.class,
                () -> rateLimiterService.checkLoginAttempt(testEmail)
            );

        // Retry-After sollte 900 Sekunden (15 Minuten) sein
        org.junit.jupiter.api.Assertions.assertEquals(900, exception.getRetryAfterSeconds());
    }

    @Test
    void testLoginRateLimiting_DifferentUsersIndependent() {
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        // User1 verbraucht alle Versuche
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(user1);
        }

        // User1 ist blockiert
        org.junit.jupiter.api.Assertions.assertTrue(rateLimiterService.isBlocked(user1));

        // User2 hat noch alle Versuche
        org.junit.jupiter.api.Assertions.assertFalse(rateLimiterService.isBlocked(user2));
        long remainingUser2 = rateLimiterService.getRemainingAttempts(user2);
        org.junit.jupiter.api.Assertions.assertEquals(5, remainingUser2);
    }

    @Test
    void testLoginRateLimiting_ExceptionMessage()  {
        String testEmail = "message@example.com";

        // Alle Versuche verbrauchen
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(testEmail);
        }

        // Exception-Message prüfen
        com.rentacar.domain.exception.TooManyLoginAttemptsException exception =
            org.junit.jupiter.api.Assertions.assertThrows(
                com.rentacar.domain.exception.TooManyLoginAttemptsException.class,
                () -> rateLimiterService.checkLoginAttempt(testEmail)
            );

        String expectedMessage = String.format(
            "Zu viele fehlgeschlagene Login-Versuche für Benutzer '%s'. Bitte versuchen Sie es in %d Sekunden erneut.",
            testEmail,
            900L
        );
        org.junit.jupiter.api.Assertions.assertEquals(expectedMessage, exception.getMessage());
    }
}


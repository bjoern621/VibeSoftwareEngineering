package com.rentacar.infrastructure.security;

import com.rentacar.domain.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für LoginRateLimiterService.
 */
class LoginRateLimiterServiceTest {

    private LoginRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new LoginRateLimiterService();
    }

    @Test
    void testAllowedLoginAttempts() {
        String username = "test@example.com";

        // Erste 5 Versuche sollten erlaubt sein
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(username));
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(username));
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(username));
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(username));
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(username));

        // 6. Versuch sollte blockiert werden
        assertThrows(TooManyLoginAttemptsException.class,
                () -> rateLimiterService.checkLoginAttempt(username));
    }

    @Test
    void testResetLoginAttempts() {
        String username = "reset@example.com";

        // 5 Versuche verbrauchen
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(username);
        }

        // Reset durchführen
        rateLimiterService.resetLoginAttempts(username);

        // Nächster Versuch sollte wieder erlaubt sein
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(username));
    }

    @Test
    void testRemainingAttempts() {
        String username = "remaining@example.com";

        // Initial sollten 5 Versuche verfügbar sein
        assertEquals(5, rateLimiterService.getRemainingAttempts(username));

        // Nach 2 Versuchen sollten 3 übrig sein
        rateLimiterService.checkLoginAttempt(username);
        rateLimiterService.checkLoginAttempt(username);
        assertEquals(3, rateLimiterService.getRemainingAttempts(username));
    }

    @Test
    void testIsBlocked() {
        String username = "blocked@example.com";

        // Initial nicht blockiert
        assertFalse(rateLimiterService.isBlocked(username));

        // Alle 5 Versuche verbrauchen
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(username);
        }

        // Jetzt sollte blockiert sein
        assertTrue(rateLimiterService.isBlocked(username));
    }

    @Test
    void testExceptionContainsRetryAfterSeconds() {
        String username = "exception@example.com";

        // Alle Versuche verbrauchen
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(username);
        }

        // Exception prüfen
        TooManyLoginAttemptsException exception = assertThrows(
                TooManyLoginAttemptsException.class,
                () -> rateLimiterService.checkLoginAttempt(username)
        );

        assertEquals(username, exception.getUsername());
        assertEquals(900, exception.getRetryAfterSeconds()); // 15 Minuten = 900 Sekunden
    }

    @Test
    void testDifferentUsersHaveSeparateLimits() {
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        // User1 verbraucht alle Versuche
        for (int i = 0; i < 5; i++) {
            rateLimiterService.checkLoginAttempt(user1);
        }

        // User1 sollte blockiert sein
        assertTrue(rateLimiterService.isBlocked(user1));

        // User2 sollte noch Versuche haben
        assertFalse(rateLimiterService.isBlocked(user2));
        assertDoesNotThrow(() -> rateLimiterService.checkLoginAttempt(user2));
    }
}


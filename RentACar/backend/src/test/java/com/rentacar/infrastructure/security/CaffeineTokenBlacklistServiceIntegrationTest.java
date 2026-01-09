package com.rentacar.infrastructure.security;

import com.rentacar.domain.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests für CaffeineTokenBlacklistService.
 * Testet die Token-Blacklist-Funktionalität mit dem echten Caffeine Cache.
 */
@SpringBootTest
class CaffeineTokenBlacklistServiceIntegrationTest {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private CaffeineTokenBlacklistService caffeineTokenBlacklistService;

    @BeforeEach
    void setUp() {
        // Blacklist vor jedem Test leeren
        tokenBlacklistService.clearBlacklist();
    }

    @Test
    void testBlacklistToken_Success() {
        // Given
        String token = "test-token-123";
        Duration ttl = Duration.ofMinutes(30);

        // When
        tokenBlacklistService.blacklistToken(token, ttl);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        assertEquals(1, caffeineTokenBlacklistService.getBlacklistSize());
    }

    @Test
    void testIsTokenBlacklisted_NotOnBlacklist() {
        // Given
        String token = "not-blacklisted-token";

        // When & Then
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void testBlacklistToken_NullToken_ShouldNotCrash() {
        // Given
        String token = null;
        Duration ttl = Duration.ofMinutes(30);

        // When
        tokenBlacklistService.blacklistToken(token, ttl);

        // Then
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
        assertEquals(0, caffeineTokenBlacklistService.getBlacklistSize());
    }

    @Test
    void testBlacklistToken_EmptyToken_ShouldNotCrash() {
        // Given
        String token = "";
        Duration ttl = Duration.ofMinutes(30);

        // When
        tokenBlacklistService.blacklistToken(token, ttl);

        // Then
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
        assertEquals(0, caffeineTokenBlacklistService.getBlacklistSize());
    }

    @Test
    void testRemoveFromBlacklist_Success() {
        // Given
        String token = "test-token-to-remove";
        Duration ttl = Duration.ofMinutes(30);
        tokenBlacklistService.blacklistToken(token, ttl);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));

        // When
        tokenBlacklistService.removeFromBlacklist(token);

        // Then
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
        assertEquals(0, caffeineTokenBlacklistService.getBlacklistSize());
    }

    @Test
    void testClearBlacklist_Success() {
        // Given
        tokenBlacklistService.blacklistToken("token1", Duration.ofMinutes(30));
        tokenBlacklistService.blacklistToken("token2", Duration.ofMinutes(30));
        tokenBlacklistService.blacklistToken("token3", Duration.ofMinutes(30));
        assertEquals(3, caffeineTokenBlacklistService.getBlacklistSize());

        // When
        tokenBlacklistService.clearBlacklist();

        // Then
        assertEquals(0, caffeineTokenBlacklistService.getBlacklistSize());
        assertFalse(tokenBlacklistService.isTokenBlacklisted("token1"));
        assertFalse(tokenBlacklistService.isTokenBlacklisted("token2"));
        assertFalse(tokenBlacklistService.isTokenBlacklisted("token3"));
    }

    @Test
    void testMultipleTokensOnBlacklist() {
        // Given
        String token1 = "token-1";
        String token2 = "token-2";
        String token3 = "token-3";
        Duration ttl = Duration.ofMinutes(30);

        // When
        tokenBlacklistService.blacklistToken(token1, ttl);
        tokenBlacklistService.blacklistToken(token2, ttl);
        tokenBlacklistService.blacklistToken(token3, ttl);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token2));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token3));
        assertEquals(3, caffeineTokenBlacklistService.getBlacklistSize());
    }

    @Test
    void testBlacklistSameTokenTwice_ShouldStillBeOne() {
        // Given
        String token = "duplicate-token";
        Duration ttl = Duration.ofMinutes(30);

        // When
        tokenBlacklistService.blacklistToken(token, ttl);
        tokenBlacklistService.blacklistToken(token, ttl);

        // Then
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        assertEquals(1, caffeineTokenBlacklistService.getBlacklistSize());
    }
}


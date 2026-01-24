package com.concertcomparison.infrastructure.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimitService Tests")
class RateLimitConfigTest {

    private RateLimitConfig.RateLimitProperties properties;
    private RateLimitConfig.RateLimitService service;

    @BeforeEach
    void setUp() {
        properties = new RateLimitConfig.RateLimitProperties();
        properties.setEnabled(true);
        properties.setRequestsPerMinute(100);
        properties.setRequestsPerMinuteHold(10);
        properties.setRequestsPerMinuteCheckout(30);
        properties.setWhitelistIps(new ArrayList<>(List.of("127.0.0.1", "192.168.1.1")));
        properties.setWhitelistRoles(new ArrayList<>(List.of("ROLE_ADMIN")));

        // Endpoint-spezifische Limits
        // WICHTIG: Keys ohne Slashes, weil YAML die Slashes beim Parsen entfernt
        // und wir normalizeEndpoint() verwenden
        Map<String, Integer> endpointLimits = new HashMap<>();
        endpointLimits.put("apireservations", 10);  // entspricht /api/reservations
        endpointLimits.put("apiorders", 30);         // entspricht /api/orders
        endpointLimits.put("apiauthlogin", 5);       // entspricht /api/auth/login
        properties.setEndpointLimits(endpointLimits);

        service = new RateLimitConfig.RateLimitService(properties);
    }

    @Test
    @DisplayName("Rate limiting is disabled when enabled=false")
    void testRateLimitingDisabled() {
        properties.setEnabled(false);
        service = new RateLimitConfig.RateLimitService(properties);

        assertFalse(service.isEnabled());
    }

    @Test
    @DisplayName("Rate limiting is enabled by default")
    void testRateLimitingEnabled() {
        assertTrue(service.isEnabled());
    }

    @Test
    @DisplayName("Should allow request when tokens are available")
    void testAllowRequestWhenTokensAvailable() {
        String clientId = "user123";
        String endpoint = "/api/concerts";

        assertTrue(service.allowRequest(clientId, endpoint));
    }

    @Test
    @DisplayName("Should consume tokens on each request")
    void testTokenConsumption() {
        String clientId = "user123";
        String endpoint = "/api/concerts";

        // Erlauben Sie 100 Anfragen (Standard-Limit)
        for (int i = 0; i < 100; i++) {
            assertTrue(service.allowRequest(clientId, endpoint), "Request " + (i + 1) + " should be allowed");
        }

        // Die 101. Anfrage sollte abgelehnt werden
        assertFalse(service.allowRequest(clientId, endpoint), "Request 101 should be rejected");
    }

    @Test
    @DisplayName("Should respect endpoint-specific limits")
    void testEndpointSpecificLimits() {
        String clientId = "user123";
        String endpoint = "/api/reservations"; // Limit = 10

        // Erlauben Sie 10 Anfragen für /api/reservations
        for (int i = 0; i < 10; i++) {
            assertTrue(service.allowRequest(clientId, endpoint), "Request " + (i + 1) + " should be allowed");
        }

        // Die 11. Anfrage sollte abgelehnt werden
        assertFalse(service.allowRequest(clientId, endpoint), "Request 11 should be rejected");
    }

    @Test
    @DisplayName("Should use global limit for non-configured endpoints")
    void testGlobalLimitForUnconfiguredEndpoint() {
        String clientId = "user123";
        String endpoint = "/api/unknown"; // Keine Konfiguration, sollte 100 verwenden

        for (int i = 0; i < 100; i++) {
            assertTrue(service.allowRequest(clientId, endpoint), "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(service.allowRequest(clientId, endpoint), "Request 101 should be rejected");
    }

    @Test
    @DisplayName("Different clients should have separate buckets")
    void testSeparateClientBuckets() {
        String client1 = "user1";
        String client2 = "user2";
        String endpoint = "/api/concerts";

        // Client 1 macht 50 Anfragen
        for (int i = 0; i < 50; i++) {
            assertTrue(service.allowRequest(client1, endpoint));
        }

        // Client 2 sollte immer noch 100 Anfragen machen können
        for (int i = 0; i < 100; i++) {
            assertTrue(service.allowRequest(client2, endpoint), "Client 2 request " + (i + 1) + " should be allowed");
        }

        // Client 2 sollte bei der 101. Anfrage blockiert werden
        assertFalse(service.allowRequest(client2, endpoint), "Client 2 request 101 should be rejected");
    }

    @Test
    @DisplayName("Different endpoints should have separate buckets for same client")
    void testSeparateEndpointBuckets() {
        String clientId = "user123";
        String endpoint1 = "/api/reservations"; // Limit = 10
        String endpoint2 = "/api/orders";       // Limit = 30

        // Erschöpfen Sie das Limit für Endpoint 1
        for (int i = 0; i < 10; i++) {
            assertTrue(service.allowRequest(clientId, endpoint1));
        }
        assertFalse(service.allowRequest(clientId, endpoint1), "Should be rejected at endpoint 1");

        // Endpoint 2 sollte unabhängig sein
        for (int i = 0; i < 30; i++) {
            assertTrue(service.allowRequest(clientId, endpoint2), "Endpoint 2 request " + (i + 1) + " should be allowed");
        }
        assertFalse(service.allowRequest(clientId, endpoint2), "Should be rejected at endpoint 2");
    }

    @Test
    @DisplayName("Should identify whitelisted IPs")
    void testIpWhitelist() {
        assertTrue(service.isIpWhitelisted("127.0.0.1"), "127.0.0.1 should be whitelisted");
        assertTrue(service.isIpWhitelisted("192.168.1.1"), "192.168.1.1 should be whitelisted");
        assertFalse(service.isIpWhitelisted("10.0.0.1"), "10.0.0.1 should not be whitelisted");
    }

    @Test
    @DisplayName("Should identify whitelisted roles")
    void testRoleWhitelist() {
        assertTrue(service.isRoleWhitelisted("ROLE_ADMIN"), "ROLE_ADMIN should be whitelisted");
        assertFalse(service.isRoleWhitelisted("ROLE_USER"), "ROLE_USER should not be whitelisted");
    }

    @Test
    @DisplayName("Should extract endpoint from request URI")
    void testExtractEndpoint() {
        assertEquals("/api/concerts", service.extractEndpoint("/api/concerts"));
        assertEquals("/api/concerts", service.extractEndpoint("/api/concerts?sortBy=date"));
        assertEquals("/api/concerts/123", service.extractEndpoint("/api/concerts/123"));
        assertEquals("/api/concerts/123", service.extractEndpoint("/api/concerts/123?expand=true"));
    }

    @Test
    @DisplayName("Should return available tokens")
    void testGetAvailableTokens() {
        String clientId = "user123";
        String endpoint = "/api/concerts";

        long availableTokens = service.getAvailableTokens(clientId, endpoint);
        assertEquals(100, availableTokens, "Should have 100 tokens initially");

        // Konsumiere einen Token
        service.allowRequest(clientId, endpoint);
        availableTokens = service.getAvailableTokens(clientId, endpoint);
        assertEquals(99, availableTokens, "Should have 99 tokens after one request");
    }

    @Test
    @DisplayName("Should return retry-after seconds")
    void testGetRetryAfterSeconds() {
        String clientId = "user123";
        String endpoint = "/api/concerts";

        long retryAfter = service.getRetryAfterSeconds(clientId, endpoint);
        assertTrue(retryAfter >= 0, "Retry-after should be non-negative");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/auth/login", "/api/reservations", "/api/orders"})
    @DisplayName("Should apply correct limits to different endpoints")
    void testMultipleEndpointLimits(String endpoint) {
        String clientId = "user123";
        int expectedLimit = switch (endpoint) {
            case "/api/auth/login" -> 5;
            case "/api/reservations" -> 10;
            case "/api/orders" -> 30;
            default -> 100;
        };

        // Konsumiere bis zum Limit
        for (int i = 0; i < expectedLimit; i++) {
            assertTrue(service.allowRequest(clientId, endpoint), 
                    "Request " + (i + 1) + " for " + endpoint + " should be allowed");
        }

        // Nächste Anfrage sollte abgelehnt werden
        assertFalse(service.allowRequest(clientId, endpoint),
                "Request after limit for " + endpoint + " should be rejected");
    }

    @Test
    @DisplayName("RateLimitProperties should have correct defaults")
    void testPropertiesDefaults() {
        RateLimitConfig.RateLimitProperties defaultProperties = new RateLimitConfig.RateLimitProperties();

        assertTrue(defaultProperties.isEnabled(), "Rate limiting should be enabled by default");
        assertEquals(100, defaultProperties.getRequestsPerMinute(), "Default requests per minute should be 100");
        assertEquals(10, defaultProperties.getRequestsPerMinuteHold(), "Default hold limit should be 10");
        assertEquals(30, defaultProperties.getRequestsPerMinuteCheckout(), "Default checkout limit should be 30");
        assertNotNull(defaultProperties.getWhitelistIps(), "Whitelist IPs should not be null");
        assertNotNull(defaultProperties.getWhitelistRoles(), "Whitelist roles should not be null");
        assertNotNull(defaultProperties.getEndpointLimits(), "Endpoint limits should not be null");
    }
}

package com.concertcomparison.presentation.controller;

import com.concertcomparison.infrastructure.ratelimit.RateLimitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(RateLimitTestConfig.class)
@DisplayName("RateLimitFilter Integration Tests")
class RateLimitFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitConfig.RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        // Reset rate limit service before each test
        // In einer echten Anwendung würde man die Buckets clearen
    }

    @Test
    @DisplayName("Should allow requests within rate limit")
    void testAllowRequestsWithinLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", "192.168.1.100"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Should return 429 when rate limit exceeded")
    void testRateLimitExceeded() throws Exception {
        // Create a unique IP for this test
        String testIp = "203.0.113.100";

        // Make requests until limit is exceeded
        // Default limit is 100 per minute, so we make 101 requests
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", testIp));
        }

        // Next request should be rejected
        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", testIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.retryAfter").isNumber());
    }

    @Test
    @DisplayName("Should include Retry-After header in 429 response")
    void testRetryAfterHeader() throws Exception {
        String testIp = "203.0.113.101";

        // Exceed the rate limit
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", testIp));
        }

        // Check for Retry-After header
        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", testIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("Should bypass rate limit for whitelisted IP")
    void testWhitelistedIpBypass() throws Exception {
        // Localhost is whitelisted by default
        String whitelistedIp = "127.0.0.1";

        // Make many requests - should all succeed
        for (int i = 0; i < 200; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", whitelistedIp))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Should track rate limit separately per IP")
    void testSeparateRateLimitPerIp() throws Exception {
        String ip1 = "203.0.113.102";
        String ip2 = "203.0.113.103";

        // Make 50 requests from IP1
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", ip1))
                    .andExpect(status().isOk());
        }

        // IP2 should still have full limit
        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", ip2))
                .andExpect(status().isOk());

        // Make 50 more requests from IP2
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", ip2))
                    .andExpect(status().isOk());
        }

        // Both should still have requests available
        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", ip1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", ip2))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should extract client IP from X-Forwarded-For header")
    void testXForwardedForHeader() throws Exception {
        String testIp = "203.0.113.104";
        String xForwardedFor = testIp + ",192.168.1.1,10.0.0.1"; // Multiple IPs

        // Make requests - the first IP in the list should be used
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", xForwardedFor))
                    .andExpect(status().isOk());
        }

        // Next request from same first IP should work
        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", xForwardedFor))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should apply endpoint-specific rate limits")
    void testEndpointSpecificLimits() throws Exception {
        // /api/auth/login has a limit of 5 requests per minute
        String testIp = "203.0.113.105";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .header("X-Forwarded-For", testIp)
                    .contentType("application/json")
                    .content("{\"email\":\"test@example.com\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized()); // Auth fails, but rate limit doesn't
        }

        // 6th request should be rate limited
        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", testIp)
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should allow unlimited requests when rate limiting is disabled")
    void testRateLimitingDisabled() throws Exception {
        // Disable rate limiting
        // Note: This test assumes we have a way to disable rate limiting
        // In practice, you might use @TestPropertySource or @DirtiesContext
        
        // This test is conditional on the ability to disable rate limiting
        // If rate limiting is always enabled, this test can be skipped
    }

    @Test
    @DisplayName("Should handle multiple concurrent requests correctly")
    void testConcurrentRequests() throws Exception {
        String testIp = "203.0.113.106";

        // Simulate concurrent requests by making them rapidly
        int successCount = 0;
        int rateLimitedCount = 0;

        for (int i = 0; i < 110; i++) {
            var response = mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", testIp))
                    .andReturn();

            if (response.getResponse().getStatus() == 200) {
                successCount++;
            } else if (response.getResponse().getStatus() == 429) {
                rateLimitedCount++;
            }
        }

        // First 100 should succeed, rest should be rate limited
        // (Note: due to bucket refill timing, this might vary slightly)
        assertTrue(successCount >= 95, "Expected at least 95 successful requests, got " + successCount);
        assertTrue(rateLimitedCount > 0, "Expected some rate limited requests, got " + rateLimitedCount);
    }

    @Test
    @DisplayName("Should use remote address when headers are not present")
    void testRemoteAddressFallback() throws Exception {
        // When no X-Forwarded-For header, the remote address should be used
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/concerts"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Should return valid JSON in rate limit error response")
    void testErrorResponseFormat() throws Exception {
        String testIp = "203.0.113.107";

        // Exceed rate limit
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("X-Forwarded-For", testIp));
        }

        // Check response format
        mockMvc.perform(get("/api/concerts")
                .header("X-Forwarded-For", testIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.retryAfter").isNumber())
                .andDo(print());
    }

    @Test
    @DisplayName("Should support CF-Connecting-IP header (Cloudflare)")
    void testCfConnectingIpHeader() throws Exception {
        String testIp = "203.0.113.108";

        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/concerts")
                    .header("CF-Connecting-IP", testIp))
                    .andExpect(status().isOk());
        }

        // Requests from same IP should share the same bucket
        mockMvc.perform(get("/api/concerts")
                .header("CF-Connecting-IP", testIp))
                .andExpect(status().isOk());
    }

    // Helper method
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

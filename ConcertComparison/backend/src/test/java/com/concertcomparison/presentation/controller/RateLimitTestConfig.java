package com.concertcomparison.presentation.controller;

import com.concertcomparison.infrastructure.ratelimit.RateLimitConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;

@TestConfiguration
public class RateLimitTestConfig {

    private final RateLimitConfig.RateLimitProperties properties;

    public RateLimitTestConfig(RateLimitConfig.RateLimitProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        properties.getEndpointLimits().put("/api/auth/login", 5);
    }
}

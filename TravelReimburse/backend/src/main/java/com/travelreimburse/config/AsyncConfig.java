package com.travelreimburse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration to enable async processing for event listeners.
 * Required for @Async annotations to work.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}


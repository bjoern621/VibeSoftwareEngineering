package com.concertcomparison.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache-Konfiguration für Concert Comparison.
 * 
 * Nutzt Caffeine (lokale In-Memory Cache) für Entwicklung.
 * Production kann auf Redis gewechselt werden.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Caffeine CacheManager für @Cacheable Annotationen.
     * 
     * Caches:
     * - concertCache: getAllConcerts() - 10 Minuten TTL
     * - seatAvailability: getSeatAvailability() - 5 Minuten TTL
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("concertCache", "seatAvailability");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }
}

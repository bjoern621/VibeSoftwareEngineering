package com.concertcomparison;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Concert Comparison Backend Application.
 * 
 * Hochskalierbares Ticket-Verkaufssystem für große Konzerte und Events.
 * Fokus auf Concurrency Control, Skalierbarkeit und DDD-Architektur.
 */
@SpringBootApplication
@EnableCaching
public class ConcertComparisonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcertComparisonApplication.class, args);
    }
}

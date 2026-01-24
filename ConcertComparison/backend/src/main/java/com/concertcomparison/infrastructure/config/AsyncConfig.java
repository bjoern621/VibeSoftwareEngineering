package com.concertcomparison.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async Configuration für asynchrone Operationen.
 * 
 * Aktiviert @Async Support für asynchrone Services.
 * 
 * HINWEIS: PaymentService wird jetzt direkt als Component registriert,
 * nicht mehr als @Bean hier, um Konflikt mit TestPaymentConfiguration zu vermeiden.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // PaymentService wird über @Component im Domain-Layer als Bean registriert
}

package com.concertcomparison.config;

import com.concertcomparison.domain.service.PaymentService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test-Konfiguration für PaymentService.
 * 
 * Stellt einen PaymentService mit festem Seed bereit,
 * sodass Tests deterministisch sind (100% Success durch testMode).
 */
@TestConfiguration
public class TestPaymentConfiguration {
    
    /**
     * PaymentService mit Seed 99999L und testMode=true.
     * 
     * - testMode=true garantiert 100% Success (siehe PaymentService.simulatePaymentOutcome)
     * - testMode=true deaktiviert 1-3s Delays (schnelle Tests)
     * - Seed ist irrelevant da testMode immer true returned
     * 
     * @return PaymentService für Tests (100% Success)
     */
    @Bean
    @Primary
    public PaymentService paymentService() {
        return new PaymentService(99999L, true);
    }
}

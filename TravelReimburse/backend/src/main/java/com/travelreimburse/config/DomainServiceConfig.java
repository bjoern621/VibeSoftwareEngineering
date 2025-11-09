package com.travelreimburse.config;

import com.travelreimburse.domain.repository.PaymentRequestRepository;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import com.travelreimburse.domain.service.PaymentInitiationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration für Domain Services.
 * Domain Services sind nicht automatisch als Beans registriert wie Application Services,
 * daher machen wir das hier explizit.
 *
 * DDD: Domain Services orchestrieren komplexe Business-Logik,
 * die nicht zu einer einzelnen Entity gehört.
 */
@Configuration
public class DomainServiceConfig {

    /**
     * Erstelle PaymentInitiationService Bean
     *
     * Spring Dependency Injection:
     * - PaymentRequestRepository wird injiziert
     * - TravelRequestRepository wird injiziert
     */
    @Bean
    public PaymentInitiationService paymentInitiationService(
            PaymentRequestRepository paymentRequestRepository,
            TravelRequestRepository travelRequestRepository) {
        return new PaymentInitiationService(paymentRequestRepository, travelRequestRepository);
    }
}


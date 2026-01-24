package com.concertcomparison.domain.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * Payment Domain Service - Mock Payment Provider.
 * 
 * Simuliert einen externen Zahlungsanbieter für verschiedene Zahlungsmethoden.
 * 
 * DDD Domain Service:
 * - Stateless Service ohne Zustandsverwaltung
 * - Business Logic für Payment-Processing
 * - Als @Service markiert für Spring Bean Registration
 * 
 * Mock-Verhalten:
 * - Random Verzögerung 1-3 Sekunden (simuliert Netzwerk-Latenz)
 * - 95% Success-Rate (5% Failure für Testing)
 * - Generiert eindeutige Transaction-IDs
 */
@Service
public class PaymentService {
    
    private static final double SUCCESS_RATE = 0.95; // 95% Erfolgsrate
    private static final int MIN_DELAY_MS = 1000; // 1 Sekunde
    private static final int MAX_DELAY_MS = 3000; // 3 Sekunden
    
    private final Random random;
    private final boolean testMode; // Deaktiviert Delay für schnelle Unit-Tests
    
    /**
     * Default Constructor mit neuem Random-Generator.
     */
    public PaymentService() {
        this.random = new Random();
        this.testMode = false;
    }
    
    /**
     * Constructor für Tests mit seed-basiertem Random (reproduzierbare Tests).
     * 
     * @param seed Seed für Random-Generator
     * @param testMode True = ohne Delay (für schnelle Unit-Tests)
     */
    public PaymentService(long seed, boolean testMode) {
        this.random = new Random(seed);
        this.testMode = testMode;
    }
    
    /**
     * Prozessiert eine Zahlung mit Mock-Payment-Provider.
     * 
     * Simuliert:
     * - Netzwerk-Latenz (1-3 Sekunden Verzögerung)
     * - Erfolgs-/Fehlerrate (95% Success, 5% Failure)
     * 
     * @return PaymentResult mit Status (SUCCESS/FAILED) und Transaction-ID
     */
    public PaymentResult processPayment() {
        // Simuliere Netzwerk-Verzögerung
        simulateNetworkDelay();
        
        // Simuliere Payment Success/Failure
        // Im testMode: 100% Success für deterministische Tests
        // Im Produktions-Modus: 95% Success-Rate
        boolean success = testMode ? true : (random.nextDouble() < SUCCESS_RATE);
        
        if (success) {
            String transactionId = generateTransactionId();
            return PaymentResult.success(transactionId);
        } else {
            return PaymentResult.failure("Payment wurde vom Zahlungsanbieter abgelehnt");
        }
    }
    
    /**
     * Generiert eine eindeutige Transaction-ID.
     * 
     * Format: TXN-{UUID}
     * Beispiel: TXN-550e8400-e29b-41d4-a716-446655440000
     * 
     * @return Eindeutige Transaction-ID
     */
    public String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString();
    }
    
    /**
     * Simuliert Netzwerk-Verzögerung zwischen 1-3 Sekunden.
     * Im Test-Modus wird der Delay übersprungen für schnelle Unit-Tests.
     */
    private void simulateNetworkDelay() {
        if (testMode) {
            return; // Kein Delay in Tests!
        }
        
        int delayMs = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS + 1);
        
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment-Processing wurde unterbrochen", e);
        }
    }
    
    /**
     * Payment Result Value Object.
     * 
     * Immutable Result-Objekt für Payment-Processing.
     */
    public static class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String errorMessage;
        
        private PaymentResult(boolean success, String transactionId, String errorMessage) {
            this.success = success;
            this.transactionId = transactionId;
            this.errorMessage = errorMessage;
        }
        
        /**
         * Erstellt ein erfolgreiches Payment-Result.
         */
        public static PaymentResult success(String transactionId) {
            if (transactionId == null || transactionId.trim().isEmpty()) {
                throw new IllegalArgumentException("TransactionId darf nicht leer sein");
            }
            return new PaymentResult(true, transactionId, null);
        }
        
        /**
         * Erstellt ein fehlgeschlagenes Payment-Result.
         */
        public static PaymentResult failure(String errorMessage) {
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("ErrorMessage darf nicht leer sein");
            }
            return new PaymentResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String toString() {
            return success 
                ? "PaymentResult{SUCCESS, txnId=" + transactionId + "}"
                : "PaymentResult{FAILED, error=" + errorMessage + "}";
        }
    }
}

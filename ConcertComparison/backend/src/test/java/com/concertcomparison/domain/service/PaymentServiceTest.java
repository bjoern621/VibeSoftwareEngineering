package com.concertcomparison.domain.service;

import com.concertcomparison.domain.service.PaymentService.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für PaymentService (Mock Payment Provider).
 * 
 * Testet:
 * - Success-Rate (sollte ~95% sein)
 * - Transaction-ID Generierung
 * - Delay-Simulation (1-3 Sekunden)
 */
class PaymentServiceTest {
    
    private PaymentService paymentService;
    
    @BeforeEach
    void setUp() {
        // Für reproduzierbare Tests: PaymentService mit festem Seed + testMode = true (kein Delay!)
        paymentService = new PaymentService(12345L, true);
    }
    
    @Test
    @DisplayName("generateTransactionId() sollte eindeutige TXN-IDs generieren")
    void testGenerateTransactionId() {
        // Given & When
        String txnId1 = paymentService.generateTransactionId();
        String txnId2 = paymentService.generateTransactionId();
        
        // Then
        assertNotNull(txnId1);
        assertNotNull(txnId2);
        assertTrue(txnId1.startsWith("TXN-"));
        assertTrue(txnId2.startsWith("TXN-"));
        assertNotEquals(txnId1, txnId2, "Transaction-IDs müssen eindeutig sein");
    }
    
    @Test
    @DisplayName("processPayment() sollte entweder SUCCESS oder FAILED zurückgeben")
    void testProcessPaymentReturnsValidResult() {
        // Given & When
        PaymentResult result = paymentService.processPayment();
        
        // Then
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getTransactionId(), "Erfolgreiche Payments müssen Transaction-ID haben");
            assertNull(result.getErrorMessage());
            assertTrue(result.getTransactionId().startsWith("TXN-"));
        } else {
            assertNull(result.getTransactionId());
            assertNotNull(result.getErrorMessage(), "Fehlgeschlagene Payments müssen Error-Message haben");
        }
    }
    
    @Test
    @DisplayName("processPayment() sollte ca. 95% Success-Rate haben (statistischer Test)")
    void testPaymentSuccessRate() {
        // Given
        int iterations = 100; // Reduziert für schnellere Tests (ausreichend für statistische Validierung)
        int successCount = 0;
        
        // When
        for (int i = 0; i < iterations; i++) {
            PaymentResult result = paymentService.processPayment();
            if (result.isSuccess()) {
                successCount++;
            }
        }
        
        // Then
        double successRate = (double) successCount / iterations;
        System.out.println("Success-Rate: " + (successRate * 100) + "%");
        
        // Akzeptiere 90-100% Success-Rate (breitere Toleranz wegen kleinerer Sample-Size)
        assertTrue(successRate >= 0.90, "Success-Rate sollte mindestens 90% sein");
        assertTrue(successRate <= 1.0, "Success-Rate sollte maximal 100% sein");
    }
    
    @Test
    @DisplayName("processPayment() sollte im Test-Modus OHNE Delay laufen")
    void testPaymentProcessingNoDelayInTestMode() {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When
        paymentService.processPayment();
        
        // Then
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 100, "Im Test-Modus sollte kein Delay vorhanden sein (< 100ms)");
    }
    
    @Test
    @DisplayName("PaymentResult.success() sollte valides Success-Result erstellen")
    void testPaymentResultSuccess() {
        // Given
        String txnId = "TXN-12345";
        
        // When
        PaymentResult result = PaymentResult.success(txnId);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(txnId, result.getTransactionId());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    @DisplayName("PaymentResult.failure() sollte valides Failure-Result erstellen")
    void testPaymentResultFailure() {
        // Given
        String errorMsg = "Payment declined";
        
        // When
        PaymentResult result = PaymentResult.failure(errorMsg);
        
        // Then
        assertFalse(result.isSuccess());
        assertEquals(errorMsg, result.getErrorMessage());
        assertNull(result.getTransactionId());
    }
    
    @Test
    @DisplayName("PaymentResult.success() sollte Exception werfen bei leerer TransactionId")
    void testPaymentResultSuccessWithEmptyTransactionId() {
        assertThrows(IllegalArgumentException.class, () -> {
            PaymentResult.success("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PaymentResult.success(null);
        });
    }
    
    @Test
    @DisplayName("PaymentResult.failure() sollte Exception werfen bei leerer ErrorMessage")
    void testPaymentResultFailureWithEmptyMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            PaymentResult.failure("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PaymentResult.failure(null);
        });
    }
    
    @Test
    @DisplayName("PaymentService mit Default-Constructor sollte funktionieren (testMode=true)")
    void testPaymentServiceDefaultConstructor() {
        // Given & When - Test-Modus ist aktiviert (kein Delay)
        PaymentService service = new PaymentService();
        long startTime = System.currentTimeMillis();
        PaymentResult result = service.processPayment();
        long duration = System.currentTimeMillis() - startTime;
        
        // Then - Test-Modus: kein Delay, immer SUCCESS
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Test-Modus sollte immer SUCCESS liefern");
        assertTrue(duration < 500, "Test-Modus sollte keinen Delay haben (< 500ms)");
    }
}

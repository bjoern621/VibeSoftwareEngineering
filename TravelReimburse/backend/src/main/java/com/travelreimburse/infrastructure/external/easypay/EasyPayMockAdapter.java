package com.travelreimburse.infrastructure.external.easypay;

import com.travelreimburse.domain.model.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Implementierung des EasyPay Adapters.
 * Simuliert die echte EasyPay API für Entwicklung und Testing.
 *
 * Verhalten:
 *  - 80% der Zahlungen erfolgreich (konfigurierbar)
 *  - 20% fehlgeschlagen
 *  - Speichert Status in Memory (nicht persistent!)
 */
@Service
public class EasyPayMockAdapter implements EasyPayAdapter {

    @Value("${easypay.mock.success-rate:0.8}")
    private double successRate;

    @Value("${easypay.mock.processing-delay-ms:100}")
    private long processingDelayMs;

    // In-Memory Storage für Mock-Daten
    private final Map<String, MockPaymentState> paymentStates = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public EasyPayResponse submitPayment(PaymentRequest paymentRequest) throws EasyPayException {
        if (paymentRequest == null) {
            throw new EasyPayException("PaymentRequest darf nicht null sein");
        }

        // Generiere eindeutige Transaction-ID
        String transactionId = generateTransactionId();

        // Simuliere Erfolg/Fehler basierend auf successRate
        boolean willSucceed = random.nextDouble() < successRate;

        // Speichere Initial-Status
        MockPaymentState state = new MockPaymentState(
            transactionId,
            paymentRequest.getId(),
            willSucceed ? EasyPayPaymentStatus.PROCESSING : EasyPayPaymentStatus.FAILED,
            System.currentTimeMillis()
        );
        paymentStates.put(transactionId, state);

        // Simuliere API-Delay
        simulateNetworkDelay();

        // Gebe Response zurück
        return new EasyPayResponse(
            transactionId,
            state.status().toString(),
            willSucceed ? "Payment erfolgreich eingereicht" : "Payment konnte nicht verarbeitet werden",
            System.currentTimeMillis()
        );
    }

    @Override
    public EasyPayPaymentStatus checkPaymentStatus(String easyPayTransactionId) throws EasyPayException {
        if (easyPayTransactionId == null || easyPayTransactionId.isBlank()) {
            throw new EasyPayException("TransactionId darf nicht null sein");
        }

        MockPaymentState state = paymentStates.get(easyPayTransactionId);
        if (state == null) {
            throw new EasyPayException("Transaktion nicht gefunden: " + easyPayTransactionId);
        }

        // Simuliere Status-Progression: PROCESSING -> SUCCESS
        if (state.status() == EasyPayPaymentStatus.PROCESSING) {
            // Nach einem kurzen Delay: Status zu SUCCESS wechseln
            if (System.currentTimeMillis() - state.createdAt() > processingDelayMs) {
                EasyPayPaymentStatus newStatus = EasyPayPaymentStatus.SUCCESS;
                paymentStates.put(easyPayTransactionId, new MockPaymentState(
                    easyPayTransactionId,
                    state.paymentRequestId(),
                    newStatus,
                    state.createdAt()
                ));
                return newStatus;
            }
        }

        return state.status();
    }

    /**
     * Generiert Mock-TransactionID
     */
    private String generateTransactionId() {
        return "EASY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    /**
     * Simuliert Netzwerk-Delay
     */
    private void simulateNetworkDelay() {
        try {
            Thread.sleep(Math.min(processingDelayMs / 2, 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Internal: Mock Payment State
     */
    private record MockPaymentState(
        String transactionId,
        Long paymentRequestId,
        EasyPayPaymentStatus status,
        long createdAt
    ) {}

    /**
     * Helper-Methode für Testing: Alle Payments reset
     */
    public void reset() {
        paymentStates.clear();
    }

    /**
     * Helper-Methode für Testing: Status manuell setzen
     */
    public void setPaymentStatus(String transactionId, EasyPayPaymentStatus status) {
        MockPaymentState oldState = paymentStates.get(transactionId);
        if (oldState != null) {
            paymentStates.put(transactionId, new MockPaymentState(
                transactionId,
                oldState.paymentRequestId(),
                status,
                oldState.createdAt()
            ));
        }
    }
}


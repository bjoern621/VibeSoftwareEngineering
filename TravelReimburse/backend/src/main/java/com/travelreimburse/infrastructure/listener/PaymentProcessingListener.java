package com.travelreimburse.infrastructure.listener;

import com.travelreimburse.application.service.PaymentService;
import com.travelreimburse.domain.event.payment.PaymentInitiatedEvent;
import com.travelreimburse.infrastructure.external.easypay.EasyPayAdapter;
import com.travelreimburse.infrastructure.external.easypay.EasyPayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Listener für Payment Processing.
 * Reagiert auf PaymentInitiatedEvent und verarbeitet Payment asynchron.
 * 
 * Flow:
 * 1. Event empfangen
 * 2. Async-Delay simulieren (EasyPay processing)
 * 3. Status bei EasyPay abfragen
 * 4. PaymentService.completePayment() aufrufen
 */
@Component
public class PaymentProcessingListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingListener.class);

    private final EasyPayAdapter easyPayAdapter;
    private final PaymentService paymentService;

    public PaymentProcessingListener(EasyPayAdapter easyPayAdapter, PaymentService paymentService) {
        this.easyPayAdapter = easyPayAdapter;
        this.paymentService = paymentService;
    }

    /**
     * Event Handler für PaymentInitiatedEvent
     * Wird asynchron ausgeführt - blockiert nicht den Haupt-Thread
     */
    @Async
    @EventListener
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        logger.info("🔄 Starting async processing for payment: {} (TravelRequest: {})", 
                    event.paymentId(), event.travelRequestId());

        try {
            // Simuliere EasyPay Processing-Delay (3-5 Sekunden)
            int delay = 3000 + (int)(Math.random() * 2000);
            logger.info("⏳ Simulating EasyPay processing delay: {}ms for payment {}", 
                        delay, event.paymentId());
            Thread.sleep(delay);

            // Status bei EasyPay abfragen
            EasyPayAdapter.EasyPayPaymentStatus status =
                easyPayAdapter.checkPaymentStatus(event.easyPayTransactionId());

            logger.info("✅ EasyPay status received for payment {}: {}", event.paymentId(), status);

            // Payment abhängig vom Status abschließen
            boolean success = status == EasyPayAdapter.EasyPayPaymentStatus.SUCCESS;
            String message = success ? "Payment processed successfully" : "Payment failed by EasyPay";

            paymentService.completePayment(event.paymentId(), success, message);

            logger.info("🎉 Payment {} completed successfully", event.paymentId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("❌ Payment processing interrupted for payment: {}", event.paymentId());
            paymentService.completePayment(event.paymentId(), false, "Processing interrupted");

        } catch (EasyPayException e) {
            logger.error("❌ EasyPay error for payment {}: {}", event.paymentId(), e.getMessage());
            paymentService.completePayment(event.paymentId(), false, "EasyPay error: " + e.getMessage());

        } catch (Exception e) {
            logger.error("❌ Unexpected error processing payment {}: {}", 
                        event.paymentId(), e.getMessage(), e);
            paymentService.completePayment(event.paymentId(), false, "Unexpected error: " + e.getMessage());
        }
    }
}


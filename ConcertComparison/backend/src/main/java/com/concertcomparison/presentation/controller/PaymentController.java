package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.PaymentApplicationService;
import com.concertcomparison.domain.model.Order;
import com.concertcomparison.domain.model.Payment;
import com.concertcomparison.domain.model.PaymentStatus;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.service.PaymentService.PaymentResult;
import com.concertcomparison.presentation.dto.PaymentRequestDTO;
import com.concertcomparison.presentation.dto.PaymentResponseDTO;
import com.concertcomparison.presentation.dto.WebhookPaymentDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller für Payment-Operationen.
 * 
 * Endpoints:
 * - POST /api/payments/process - Payment asynchron triggern
 * - POST /api/payments/webhook - Webhook-Simulation (Internal/Admin)
 * - GET /api/payments/status/{orderId} - Payment-Status abfragen
 * 
 * Security: USER role für alle Endpoints (außer Webhook: ADMIN)
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentApplicationService paymentApplicationService;
    private final OrderRepository orderRepository;
    
    public PaymentController(
            PaymentApplicationService paymentApplicationService,
            OrderRepository orderRepository) {
        this.paymentApplicationService = paymentApplicationService;
        this.orderRepository = orderRepository;
    }
    
    /**
     * POST /api/payments/process
     * 
     * Triggert asynchrones Payment-Processing für eine Order.
     * 
     * Flow:
     * 1. Order muss existieren und PENDING sein
     * 2. Payment wird asynchron prozessiert (1-3s Delay, 95% Success)
     * 3. Client bekommt sofort Response "PROCESSING"
     * 4. Nach Abschluss: Status via GET /api/payments/status/{orderId} abfragen
     * 
     * @param request PaymentRequestDTO mit orderId
     * @return PaymentResponseDTO mit Status PENDING
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        logger.info("POST /api/payments/process - orderId={}, paymentMethod={}", 
            request.getOrderId(), request.getPaymentMethod());
        
        try {
            // 1. Order validieren
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Order " + request.getOrderId() + " nicht gefunden"));
            
            if (order.getStatus() != com.concertcomparison.domain.model.OrderStatus.PENDING) {
                return ResponseEntity.badRequest()
                    .body(PaymentResponseDTO.failed(
                        request.getOrderId(),
                        "Order ist nicht im Status PENDING"
                    ));
            }
            
            // 2. Payment asynchron triggern
            CompletableFuture<PaymentResult> futureResult = 
                paymentApplicationService.processPaymentAsync(request.getOrderId());
            
            // 3. Sofort Response zurückgeben (Processing)
            PaymentResponseDTO response = PaymentResponseDTO.processing(request.getOrderId());
            
            logger.info("Payment processing started for orderId={}", request.getOrderId());
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            logger.error("Payment processing failed for orderId={}", request.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentResponseDTO.failed(
                    request.getOrderId(),
                    "Technischer Fehler: " + e.getMessage()
                ));
        }
    }
    
    /**
     * GET /api/payments/status/{orderId}
     * 
     * Fragt den aktuellen Payment-Status einer Order ab.
     * 
     * Status:
     * - PENDING: Payment wird verarbeitet
     * - COMPLETED: Payment erfolgreich
     * - FAILED: Payment fehlgeschlagen
     * 
     * @param orderId Order-ID
     * @return PaymentResponseDTO mit aktuellem Status
     */
    @GetMapping("/status/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatus(@PathVariable Long orderId) {
        logger.info("GET /api/payments/status/{}", orderId);
        
        try {
            // Order laden
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Order " + orderId + " nicht gefunden"));
            
            Payment payment = order.getPayment();
            if (payment == null) {
                return ResponseEntity.badRequest()
                    .body(PaymentResponseDTO.failed(orderId, "Order hat kein Payment"));
            }
            
            // Response basierend auf Payment-Status
            PaymentResponseDTO response;
            switch (payment.getStatus()) {
                case COMPLETED:
                    response = PaymentResponseDTO.success(orderId, payment.getTransactionId());
                    break;
                case FAILED:
                    response = PaymentResponseDTO.failed(orderId, "Zahlung wurde abgelehnt");
                    break;
                case PENDING:
                    response = PaymentResponseDTO.processing(orderId);
                    break;
                case CANCELLED:
                    response = PaymentResponseDTO.failed(orderId, "Zahlung wurde storniert");
                    break;
                default:
                    response = new PaymentResponseDTO(
                        orderId,
                        payment.getStatus(),
                        "Status: " + payment.getStatus().getDisplayName()
                    );
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get payment status for orderId={}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentResponseDTO.failed(orderId, "Fehler beim Abrufen des Status"));
        }
    }
    
    /**
     * POST /api/payments/webhook
     * 
     * Webhook-Endpoint für asynchrone Payment-Notifications.
     * 
     * Simuliert externe Webhook-Benachrichtigung vom Payment-Provider.
     * In einer echten Implementierung würde dieser Endpoint von extern aufgerufen.
     * 
     * Security: Nur ADMIN (in Produktion: API-Key-Validierung)
     * 
     * @param webhookData Webhook-Daten mit orderId, status, transactionId
     * @return Response mit acknowledged=true
     */
    @PostMapping("/webhook")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> handleWebhook(@Valid @RequestBody WebhookPaymentDTO webhookData) {
        logger.info("POST /api/payments/webhook - orderId={}, status={}, txnId={}", 
            webhookData.getOrderId(), 
            webhookData.getStatus(), 
            webhookData.getTransactionId());
        
        try {
            // Webhook-Daten verarbeiten
            if (webhookData.getStatus() == PaymentStatus.COMPLETED) {
                // Success
                paymentApplicationService.handlePaymentSuccess(
                    webhookData.getOrderId(),
                    webhookData.getTransactionId()
                );
            } else if (webhookData.getStatus() == PaymentStatus.FAILED) {
                // Failure
                paymentApplicationService.handlePaymentFailure(
                    webhookData.getOrderId(),
                    webhookData.getErrorMessage() != null 
                        ? webhookData.getErrorMessage() 
                        : "Payment failed"
                );
            } else {
                logger.warn("Unhandled webhook status: {}", webhookData.getStatus());
            }
            
            return ResponseEntity.ok()
                .body(new WebhookResponse(true, "Webhook verarbeitet"));
            
        } catch (Exception e) {
            logger.error("Webhook processing failed for orderId={}", webhookData.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new WebhookResponse(false, "Webhook-Verarbeitung fehlgeschlagen"));
        }
    }
    
    /**
     * Simple Response DTO für Webhook.
     */
    private static class WebhookResponse {
        private final boolean acknowledged;
        private final String message;
        
        public WebhookResponse(boolean acknowledged, String message) {
            this.acknowledged = acknowledged;
            this.message = message;
        }
        
        public boolean isAcknowledged() {
            return acknowledged;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

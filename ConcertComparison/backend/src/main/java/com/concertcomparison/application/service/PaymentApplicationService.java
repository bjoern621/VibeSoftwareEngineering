package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.OrderNotFoundException;
import com.concertcomparison.domain.exception.SeatNotFoundException;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.domain.service.PaymentService;
import com.concertcomparison.domain.service.PaymentService.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Application Service für Payment-Operationen.
 * 
 * Orchestriert den asynchronen Payment-Flow:
 * - Payment Processing mit Mock Provider
 * - Success-Handling: Order bestätigen, Reservation löschen
 * - Failure-Handling: Rollback (Order cancel, Seat SOLD → HELD, neue Reservation)
 * 
 * DDD Application Service:
 * - Orchestriert Domain Entities und Services
 * - Verwaltet Transaktionen
 * - Keine Business Logic (delegiert an Domain Layer)
 */
@Service
public class PaymentApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentApplicationService.class);
    private static final int ROLLBACK_HOLD_DURATION_MINUTES = 5; // 5 Minuten für Re-Payment
    
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final boolean syncMode; // Für Tests: Synchrone Payment-Verarbeitung
    
    public PaymentApplicationService(
            PaymentService paymentService,
            OrderRepository orderRepository,
            SeatRepository seatRepository,
            ReservationRepository reservationRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.syncMode = false; // Wird nicht mehr genutzt, Payment immer SYNC für Tests
    }

    /**
     * Prozessiert Payment asynchron für eine Order.
     * 
     * Flow:
     * 1. Order laden und validieren
     * 2. Payment mit Mock Provider prozessieren (1-3s Delay, 95% Success)
     * 3. Bei Success: handlePaymentSuccess()
     * 4. Bei Failure: handlePaymentFailure() mit Rollback
     * 
     * @param orderId ID der Order
     * @return CompletableFuture mit PaymentResult
     */
    @Async
    @Transactional
    public CompletableFuture<PaymentResult> processPaymentAsync(Long orderId) {
        logger.info("Starting async payment processing for orderId={}", orderId);
        
        try {
            // 1. Order laden
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
            
            // 2. Validierung: Order muss PENDING sein
            if (order.getStatus() != OrderStatus.PENDING) {
                logger.warn("Payment processing failed: Order {} is not PENDING (status={})", 
                    orderId, order.getStatus());
                throw new IllegalStateException(
                    String.format("Order %d ist nicht im Status PENDING (aktuell: %s)", 
                        orderId, order.getStatus())
                );
            }
            
            // 3. Validierung: Payment muss PENDING sein
            Payment payment = order.getPayment();
            if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
                logger.warn("Payment processing failed: Payment is not PENDING for orderId={}", orderId);
                throw new IllegalStateException(
                    String.format("Payment für Order %d ist nicht im Status PENDING", orderId)
                );
            }
            
            // 4. Payment mit Mock Provider prozessieren
            logger.info("Processing payment with Mock Provider for orderId={}, method={}", 
                orderId, payment.getMethod());
            PaymentResult result = paymentService.processPayment();
            
            // 5. Result verarbeiten
            if (result.isSuccess()) {
                handlePaymentSuccess(orderId, result.getTransactionId());
                logger.info("Payment SUCCESS for orderId={}, txnId={}", orderId, result.getTransactionId());
            } else {
                handlePaymentFailure(orderId, result.getErrorMessage());
                logger.warn("Payment FAILED for orderId={}, error={}", orderId, result.getErrorMessage());
            }
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("Payment processing exception for orderId={}", orderId, e);
            // Bei Exception auch Rollback durchführen
            try {
                handlePaymentFailure(orderId, "Technischer Fehler: " + e.getMessage());
            } catch (Exception rollbackException) {
                logger.error("Rollback failed for orderId={}", orderId, rollbackException);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Synchrone Variante für direktes Payment-Processing (z.B. für Webhooks).
     * 
     * @param orderId ID der Order
     * @return PaymentResult
     */
    @Transactional
    public PaymentResult processPaymentSync(Long orderId) {
        logger.info("Starting sync payment processing for orderId={}", orderId);
        
        // Order laden
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Validierungen
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Order %d ist nicht im Status PENDING", orderId)
            );
        }
        
        Payment payment = order.getPayment();
        if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                String.format("Payment für Order %d ist nicht im Status PENDING", orderId)
            );
        }
        
        // Payment prozessieren
        PaymentResult result = paymentService.processPayment();
        
        // Result verarbeiten
        if (result.isSuccess()) {
            handlePaymentSuccess(orderId, result.getTransactionId());
        } else {
            handlePaymentFailure(orderId, result.getErrorMessage());
        }
        
        return result;
    }
    
    /**
     * Verarbeitet erfolgreiches Payment.
     * 
     * Actions:
     * 1. Order.completePayment() → Payment COMPLETED, Order CONFIRMED
     * 2. Reservation löschen (nicht mehr benötigt)
     * 
     * @param orderId ID der Order
     * @param transactionId Transaction-ID vom Payment Provider
     */
    @Transactional
    public void handlePaymentSuccess(Long orderId, String transactionId) {
        logger.info("Handling payment success for orderId={}, txnId={}", orderId, transactionId);
        
        // 1. Order laden
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // 2. Payment completen und Order bestätigen (via Aggregate Root)
        order.completePayment(transactionId);
        orderRepository.save(order);
        
        // 3. Reservation löschen (Hold wird nicht mehr benötigt)
        if (order.getReservationId() != null) {
            reservationRepository.findById(order.getReservationId())
                .ifPresent(reservation -> {
                    reservationRepository.delete(reservation);
                    logger.info("Deleted reservation {} for successful payment", order.getReservationId());
                });
        }
        
        logger.info("Payment success handling completed for orderId={}", orderId);
    }
    
    /**
     * Verarbeitet fehlgeschlagenes Payment mit vollständigem Rollback.
     * 
     * Rollback Actions:
     * 1. Order.failPayment() → Payment FAILED, Order CANCELLED
     * 2. Seat SOLD → HELD (rollbackToHeld)
     * 3. Neue Reservation erstellen (5 Min TTL für Re-Payment-Versuch)
     * 
     * @param orderId ID der Order
     * @param errorMessage Fehlermeldung vom Payment Provider
     */
    @Transactional
    public void handlePaymentFailure(Long orderId, String errorMessage) {
        logger.warn("Handling payment failure for orderId={}, error={}", orderId, errorMessage);
        
        try {
            // 1. Order laden
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
            
            // 2. Payment failen und Order canceln (via Aggregate Root)
            order.failPayment();
            orderRepository.save(order);
            
            // 3. Seat laden
            Seat seat = seatRepository.findById(order.getSeatId())
                .orElseThrow(() -> new SeatNotFoundException(order.getSeatId()));
            
            // 4. Seat SOLD → HELD rollback
            String newReservationId = "ROLLBACK-" + orderId;
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ROLLBACK_HOLD_DURATION_MINUTES);
            
            seat.rollbackToHeld(newReservationId, expiresAt);
            seatRepository.save(seat);
            
            logger.info("Seat {} rolled back: SOLD → HELD (reservationId={})", 
                seat.getId(), newReservationId);
            
            // 5. Neue Reservation erstellen (für Re-Payment-Versuch)
            Reservation newReservation = Reservation.createHold(
                seat.getId(),
                order.getUserId(),
                ROLLBACK_HOLD_DURATION_MINUTES
            );
            reservationRepository.save(newReservation);
            
            logger.info("Created new reservation {} for rollback (orderId={}, duration={}min)", 
                newReservation.getId(), orderId, ROLLBACK_HOLD_DURATION_MINUTES);
            
            logger.info("Payment failure rollback completed for orderId={}", orderId);
            
        } catch (Exception e) {
            logger.error("Rollback failed for orderId={}", orderId, e);
            throw new RuntimeException("Payment-Rollback fehlgeschlagen für Order " + orderId, e);
        }
    }
}

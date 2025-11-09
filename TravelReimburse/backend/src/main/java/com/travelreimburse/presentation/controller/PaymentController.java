package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.service.PaymentService;
import com.travelreimburse.presentation.dto.EasyPayCallbackDTO;
import com.travelreimburse.presentation.dto.PaymentRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller für Payment-Management
 * KEINE @PreAuthorize - alle Endpoints sind offen
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Zahlungen", description = "EasyPay Zahlungsverwaltung")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments/travel-requests/{travelRequestId}/submit
     * Erstellt einen Payment und sendet ihn zu EasyPay
     */
    @PostMapping("/travel-requests/{travelRequestId}/submit")
    @Operation(summary = "Payment erstellen und zu EasyPay senden")
    public ResponseEntity<PaymentRequestDTO> createAndSubmitPayment(
            @PathVariable Long travelRequestId) {
        PaymentRequestDTO payment = paymentService.createAndSubmitPayment(travelRequestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * GET /api/payments/{paymentId}
     * Ruft den Status eines einzelnen Payments ab
     */
    @GetMapping("/{paymentId}")
    @Operation(summary = "Payment-Status abrufen")
    public ResponseEntity<PaymentRequestDTO> getPaymentStatus(@PathVariable Long paymentId) {
        PaymentRequestDTO payment = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * GET /api/payments/pending
     * Ruft alle ausstehenden Zahlungen ab
     */
    @GetMapping("/pending")
    @Operation(summary = "Alle ausstehenden Zahlungen")
    public ResponseEntity<List<PaymentRequestDTO>> getPendingPayments() {
        List<PaymentRequestDTO> payments = paymentService.findPendingPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments/failed
     * Ruft alle fehlgeschlagenen Zahlungen ab
     */
    @GetMapping("/failed")
    @Operation(summary = "Alle fehlgeschlagenen Zahlungen")
    public ResponseEntity<List<PaymentRequestDTO>> getFailedPayments() {
        List<PaymentRequestDTO> payments = paymentService.findFailedPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * POST /api/payments/{paymentId}/retry
     * Wiederholt eine fehlgeschlagene Zahlung
     */
    @PostMapping("/{paymentId}/retry")
    @Operation(summary = "Fehlgeschlagene Zahlung wiederholen")
    public ResponseEntity<PaymentRequestDTO> retryFailedPayment(@PathVariable Long paymentId) {
        PaymentRequestDTO payment = paymentService.retryFailedPayment(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * GET /api/payments/by-reference/{reference}
     * Sucht einen Payment nach Referenz
     */
    @GetMapping("/by-reference/{reference}")
    @Operation(summary = "Payment nach Referenz suchen")
    public ResponseEntity<PaymentRequestDTO> findByReference(@PathVariable String reference) {
        PaymentRequestDTO payment = paymentService.findByPaymentReference(reference);
        return ResponseEntity.ok(payment);
    }

    /**
     * POST /api/payments/callback
     * EasyPay sendet Zahlungsstatus-Update (INTERNAL)
     *
     * Diese Endpoint ist für EasyPay Mock - später würde EasyPay hier ihre Callbacks senden
     */
    @PostMapping("/callback")
    @Operation(summary = "EasyPay sendet Zahlungsstatus-Update")
    public ResponseEntity<Void> handleEasyPayCallback(@Valid @RequestBody EasyPayCallbackDTO callback) {
        paymentService.handlePaymentCallback(
            callback.transactionId(),
            callback.status(),
            callback.reason()
        );
        return ResponseEntity.ok().build();
    }
}


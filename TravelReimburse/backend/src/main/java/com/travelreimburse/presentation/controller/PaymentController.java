package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.service.PaymentService;
import com.travelreimburse.presentation.dto.PaymentRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * GET /api/payments/by-reference/{reference}
     * Sucht einen Payment nach Referenz
     */
    @GetMapping("/by-reference/{reference}")
    @Operation(summary = "Payment nach Referenz suchen")
    public ResponseEntity<PaymentRequestDTO> findByReference(@PathVariable String reference) {
        PaymentRequestDTO payment = paymentService.findByPaymentReference(reference);
        return ResponseEntity.ok(payment);
    }

}


package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PaymentRequest Entity - Aggregate Root für Zahlungsaufträge.
 * Verwaltet den Zahlungsprozess mit EasyPay.
 */
@Entity
@Table(name = "payment_requests")
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_request_id", nullable = false)
    private TravelRequest travelRequest;

    @Embedded
    private Money totalAmount;

    @Embedded
    private PaymentReference paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    private LocalDateTime completedAt;

    @Column(name = "easy_pay_transaction_id")
    private String easyPayTransactionId;

    @Column(length = 1000)
    private String failureReason;

    protected PaymentRequest() {
        // JPA
    }

    /**
     * Privater Konstruktor - wird durch Factory-Methoden verwendet
     */
    private PaymentRequest(TravelRequest travelRequest, Money totalAmount) {
        if (travelRequest == null) {
            throw new IllegalArgumentException("TravelRequest darf nicht null sein");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("Gesamtbetrag darf nicht null sein");
        }

        this.travelRequest = travelRequest;
        this.totalAmount = totalAmount;
        this.paymentReference = PaymentReference.generate();
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory-Methode: Erstellt einen neuen Payment-Request im Status PENDING
     */
    public static PaymentRequest create(TravelRequest travelRequest, Money totalAmount) {
        return new PaymentRequest(travelRequest, totalAmount);
    }

    /**
     * Business-Methode: Markiert Payment als erfolgreich
     * EINFACHER FLOW: PENDING → SUCCESS (EasyPay ist gemockt!)
     *
     * Invarianten:
     *  - Status muss PENDING sein
     *  - transactionId darf nicht null sein
     */
    public void markAsSuccess(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                String.format("Nur Payments im Status PENDING können erfolgreich sein (aktuell: %s)", this.status)
            );
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("EasyPay TransactionId darf nicht null oder leer sein");
        }

        this.status = PaymentStatus.SUCCESS;
        this.easyPayTransactionId = transactionId;
        this.submittedAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Markiert Payment als fehlgeschlagen
     * EINFACHER FLOW: PENDING → FAILED (EasyPay ist gemockt!)
     *
     * Invarianten:
     *  - Status muss PENDING sein
     *  - failureReason darf nicht null sein
     */
    public void markAsFailed(String failureReason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                String.format("Nur Payments im Status PENDING können fehlschlagen (aktuell: %s)", this.status)
            );
        }
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("Fehlschlagsgrund darf nicht null oder leer sein");
        }

        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.submittedAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Business-Query: Kann Payment übermittelt werden?
     */
    public boolean canBeSubmitted() {
        return this.status == PaymentStatus.PENDING;
    }

    /**
     * Business-Query: War Payment erfolgreich?
     */
    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCESS;
    }

    /**
     * Business-Query: Ist Payment fehlgeschlagen?
     */
    public boolean hasFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    /**
     * Business-Query: Kann Payment wiederholt werden?
     */
    public boolean canBeRetried() {
        return this.status == PaymentStatus.FAILED;
    }

    // ====== GETTER (nur lesender Zugriff) ======

    public Long getId() {
        return id;
    }

    public TravelRequest getTravelRequest() {
        return travelRequest;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public PaymentReference getPaymentReference() {
        return paymentReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getEasyPayTransactionId() {
        return easyPayTransactionId;
    }

    public String getFailureReason() {
        return failureReason;
    }
}


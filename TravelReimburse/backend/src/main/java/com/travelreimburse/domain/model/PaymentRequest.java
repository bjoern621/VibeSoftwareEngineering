package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Column(nullable = false)
    private boolean archived = false;

    @Column
    private LocalDateTime archivedAt;

    @Column
    private LocalDate retentionUntil;

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
     * Business-Methode: Markiert Payment als eingereicht (an EasyPay gesendet)
     * Flow: PENDING → PROCESSING
     * Invarianten:
     *  - Status muss PENDING sein
     *  - transactionId darf nicht null sein
     */
    public void markAsProcessing(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                String.format("Nur Payments im Status PENDING können eingereicht werden (aktuell: %s)", this.status)
            );
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("EasyPay TransactionId darf nicht null oder leer sein");
        }

        this.status = PaymentStatus.PROCESSING;
        this.easyPayTransactionId = transactionId;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Markiert Payment als erfolgreich
     * Flow: PROCESSING → SUCCESS
     * Invarianten:
     *  - Status muss PROCESSING sein
     */
    public void markAsSuccess() {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new IllegalArgumentException(
                String.format("Nur Payments im Status PROCESSING können erfolgreich sein (aktuell: %s)", this.status)
            );
        }

        this.status = PaymentStatus.SUCCESS;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Markiert Payment als fehlgeschlagen
     * Flow: PENDING/PROCESSING → FAILED
     * Invarianten:
     *  - Status muss PENDING oder PROCESSING sein
     *  - failureReason darf nicht null sein
     */
    public void markAsFailed(String failureReason) {
        if (this.status != PaymentStatus.PENDING && this.status != PaymentStatus.PROCESSING) {
            throw new IllegalArgumentException(
                String.format("Nur Payments im Status PENDING oder PROCESSING können fehlschlagen (aktuell: %s)", this.status)
            );
        }
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("Fehlschlagsgrund darf nicht null oder leer sein");
        }

        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Business-Query: Kann Payment übermittelt werden?
     */
    public boolean canBeSubmitted() {
        return this.status == PaymentStatus.PENDING;
    }

    /**
     * Business-Query: Wird Payment gerade bearbeitet?
     */
    public boolean isProcessing() {
        return this.status == PaymentStatus.PROCESSING;
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

    /**
     * Business-Methode: Archiviert den PaymentRequest
     * Invarianten:
     *  - Status muss SUCCESS oder FAILED sein (Payment abgeschlossen)
     *  - Darf nicht bereits archiviert sein
     *  - retentionYears muss >= 1 sein
     */
    public void archive(int retentionYears) {
        if (!isCompleted()) {
            throw new IllegalStateException(
                String.format("Nur abgeschlossene Payments können archiviert werden (aktuell: %s)", this.status)
            );
        }
        if (this.archived) {
            throw new IllegalStateException("Payment ist bereits archiviert");
        }
        if (retentionYears < 1) {
            throw new IllegalArgumentException("Aufbewahrungsfrist muss mindestens 1 Jahr betragen");
        }

        this.archived = true;
        this.archivedAt = LocalDateTime.now();
        this.retentionUntil = LocalDate.now().plusYears(retentionYears);
    }

    /**
     * Business-Query: Ist Payment abgeschlossen?
     */
    public boolean isCompleted() {
        return this.status == PaymentStatus.SUCCESS || this.status == PaymentStatus.FAILED;
    }

    /**
     * Business-Query: Kann archiviert werden?
     */
    public boolean canBeArchived() {
        return isCompleted() && !this.archived;
    }

    /**
     * Business-Query: Ist Aufbewahrungsfrist abgelaufen?
     */
    public boolean isRetentionExpired() {
        return this.archived
            && this.retentionUntil != null
            && LocalDate.now().isAfter(this.retentionUntil);
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

    public boolean isArchived() {
        return archived;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public LocalDate getRetentionUntil() {
        return retentionUntil;
    }
}


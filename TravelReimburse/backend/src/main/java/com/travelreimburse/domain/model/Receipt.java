package com.travelreimburse.domain.model;

import com.travelreimburse.domain.event.receipt.ReceiptStatusChangedEvent;
import jakarta.persistence.*;
import org.springframework.data.domain.AbstractAggregateRoot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Receipt Entity - Repräsentiert einen digitalen Beleg.
 * Gehört immer zu einem TravelRequest (Aggregate Root).
 * DDD: Extended from AbstractAggregateRoot to support domain events.
 */
@Entity
@Table(name = "receipts")
public class Receipt extends AbstractAggregateRoot<Receipt> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_request_id", nullable = false)
    private TravelRequest travelRequest;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptType type;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(length = 500)
    private String description;

    @Embedded
    private Money amount;

    @Column(length = 100)
    private String vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime validatedAt;

    @Column(length = 1000)
    private String rejectionReason;

    // JPA Constructor
    protected Receipt() {
    }

    // Business Constructor
    public Receipt(TravelRequest travelRequest, String fileName, String originalFileName,
                   String filePath, String contentType, Long fileSize, ReceiptType type,
                   LocalDate issueDate, String description, Money amount, String vendor) {
        validateConstructorParameters(travelRequest, fileName, originalFileName, filePath,
                contentType, fileSize, type, issueDate);

        this.travelRequest = travelRequest;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.type = type;
        this.issueDate = issueDate;
        this.description = description;
        this.amount = amount;
        this.vendor = vendor;
        this.status = ReceiptStatus.UPLOADED;
        this.uploadedAt = LocalDateTime.now();
    }

    // Validierung der Constructor-Parameter
    private void validateConstructorParameters(TravelRequest travelRequest, String fileName,
                                                String originalFileName, String filePath,
                                                String contentType, Long fileSize,
                                                ReceiptType type, LocalDate issueDate) {
        if (travelRequest == null) {
            throw new IllegalArgumentException("TravelRequest darf nicht null sein");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Dateiname darf nicht leer sein");
        }
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Original-Dateiname darf nicht leer sein");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Dateipfad darf nicht leer sein");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content-Type darf nicht leer sein");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("Dateigröße muss positiv sein");
        }
        if (type == null) {
            throw new IllegalArgumentException("Belegtyp darf nicht null sein");
        }
        if (issueDate == null) {
            throw new IllegalArgumentException("Ausstellungsdatum darf nicht null sein");
        }
        if (issueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ausstellungsdatum darf nicht in der Zukunft liegen");
        }
    }

    /**
     * Business-Methode: Beleg als validiert markieren.
     * DDD: Publishes domain event for side-effects (email notifications).
     *
     * @throws IllegalStateException if receipt is not in UPLOADED status
     */
    public void validate() {
        if (this.status != ReceiptStatus.UPLOADED) {
            throw new IllegalStateException("Nur hochgeladene Belege können validiert werden");
        }

        ReceiptStatus oldStatus = this.status;
        this.status = ReceiptStatus.VALIDATED;
        this.validatedAt = LocalDateTime.now();

        // Register domain event
        registerEvent(new ReceiptStatusChangedEvent(
            this.id,
            oldStatus,
            ReceiptStatus.VALIDATED
        ));
    }

    /**
     * Business-Methode: Beleg ablehnen.
     * DDD: Publishes domain event for side-effects.
     *
     * @param reason Reason for rejection
     * @throws IllegalArgumentException if reason is blank
     * @throws IllegalStateException if receipt is already archived
     */
    public void reject(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Ablehnungsgrund darf nicht leer sein");
        }
        if (this.status == ReceiptStatus.ARCHIVED) {
            throw new IllegalStateException("Archivierte Belege können nicht abgelehnt werden");
        }

        ReceiptStatus oldStatus = this.status;
        this.status = ReceiptStatus.REJECTED;
        this.rejectionReason = reason;

        // Register domain event
        registerEvent(new ReceiptStatusChangedEvent(
            this.id,
            oldStatus,
            ReceiptStatus.REJECTED
        ));
    }

    /**
     * Business-Methode: Beleg archivieren.
     * DDD: Publishes domain event for archival tracking.
     *
     * @throws IllegalStateException if receipt is already archived
     */
    public void archive() {
        if (this.status == ReceiptStatus.ARCHIVED) {
            throw new IllegalStateException("Beleg ist bereits archiviert");
        }

        ReceiptStatus oldStatus = this.status;
        this.status = ReceiptStatus.ARCHIVED;

        // Register domain event
        registerEvent(new ReceiptStatusChangedEvent(
            this.id,
            oldStatus,
            ReceiptStatus.ARCHIVED
        ));
    }

    /**
     * Prüft ob der Beleg gültig ist (validiert und nicht abgelehnt)
     */
    public boolean isValid() {
        return this.status == ReceiptStatus.VALIDATED;
    }

    /**
     * Prüft ob der Beleg innerhalb des Reisezeitraums ausgestellt wurde
     */
    public boolean isWithinTravelPeriod() {
        DateRange travelPeriod = travelRequest.getTravelPeriod();
        return !issueDate.isBefore(travelPeriod.getStartDate()) &&
                !issueDate.isAfter(travelPeriod.getEndDate());
    }

    // Getters
    public Long getId() {
        return id;
    }

    public TravelRequest getTravelRequest() {
        return travelRequest;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public ReceiptType getType() {
        return type;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public String getDescription() {
        return description;
    }

    public Money getAmount() {
        return amount;
    }

    public String getVendor() {
        return vendor;
    }

    public ReceiptStatus getStatus() {
        return status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    // Setters für beschreibende Felder
    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return Objects.equals(id, receipt.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", amount=" + amount +
                '}';
    }
}


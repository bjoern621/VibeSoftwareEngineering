package com.travelreimburse.domain.model;

import com.travelreimburse.domain.event.travelrequest.TravelRequestStatusChangedEvent;
import com.travelreimburse.domain.exception.CannotArchiveTravelRequestException;
import com.travelreimburse.domain.exception.InvalidStatusTransitionException;
import com.travelreimburse.domain.exception.InvalidTravelRequestDataException;
import com.travelreimburse.domain.exception.InvalidTravelRequestStateException;
import jakarta.persistence.*;
import org.springframework.data.domain.AbstractAggregateRoot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root für Reiseanträge
 * Verwaltet alle Informationen zu einer beantragten Dienstreise inklusive Reiserouten
 */
@Entity
@Table(name = "travel_requests")
public class TravelRequest extends AbstractAggregateRoot<TravelRequest> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
    @Embedded
    private CostCenter costCenter;

    @Column(nullable = false, length = 500)
    private String destination;
    
    @Column(nullable = false, length = 1000)
    private String purpose;
    
    @Embedded
    private DateRange travelPeriod;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "estimated_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "estimated_currency"))
    })
    private Money estimatedCost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelRequestStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime submittedAt;
    
    @Column
    private Long approverId;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime rejectedAt;

    @Column(length = 1000)
    private String rejectionReason;
    
    @Embedded
    private RetentionPeriod retentionPeriod;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_request_id", unique = true)
    private PaymentRequest paymentRequest;

    @OneToMany(mappedBy = "travelRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TravelLeg> travelLegs = new ArrayList<>();

    // JPA benötigt Default-Konstruktor
    protected TravelRequest() {
    }
    
    /**
     * Erstellt einen neuen Reiseantrag im Status DRAFT
     */
    public TravelRequest(Long employeeId, CostCenter costCenter, String destination, String purpose,
                         DateRange travelPeriod, Money estimatedCost) {
        if (employeeId == null) {
            throw new InvalidTravelRequestDataException("employeeId", "darf nicht null sein");
        }
        if (costCenter == null) {
            throw new InvalidTravelRequestDataException("costCenter", "darf nicht null sein");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new InvalidTravelRequestDataException("destination", "darf nicht leer sein");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new InvalidTravelRequestDataException("purpose", "darf nicht leer sein");
        }
        if (travelPeriod == null) {
            throw new InvalidTravelRequestDataException("travelPeriod", "darf nicht null sein");
        }
        if (estimatedCost == null) {
            throw new InvalidTravelRequestDataException("estimatedCost", "darf nicht null sein");
        }
        
        this.employeeId = employeeId;
        this.costCenter = costCenter;
        this.destination = destination;
        this.purpose = purpose;
        this.travelPeriod = travelPeriod;
        this.estimatedCost = estimatedCost;
        this.status = TravelRequestStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Reiseantrag einreichen
     * Zustandsübergang: DRAFT -> SUBMITTED
     */
    public void submit() {
        if (status != TravelRequestStatus.DRAFT) {
            throw new InvalidTravelRequestStateException(status, "submit");
        }
        this.status = TravelRequestStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
    
    /**
     * Business-Methode: Reiseantrag genehmigen
     * Zustandsübergang: SUBMITTED -> APPROVED
     *
     * @param approverId ID der genehmigenden Führungskraft
     */
    public void approve(Long approverId) {
        if (approverId == null) {
            throw new InvalidTravelRequestDataException("approverId", "darf nicht null sein");
        }
        if (status != TravelRequestStatus.SUBMITTED) {
            throw new InvalidTravelRequestStateException(status, "approve");
        }
        this.status = TravelRequestStatus.APPROVED;
        this.approverId = approverId;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Business-Methode: Reiseantrag ablehnen
     * Zustandsübergang: SUBMITTED -> REJECTED
     *
     * @param approverId ID der ablehnenden Führungskraft
     * @param reason Grund für die Ablehnung
     */
    public void reject(Long approverId, String reason) {
        if (approverId == null) {
            throw new InvalidTravelRequestDataException("approverId", "darf nicht null sein");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidTravelRequestDataException("rejectionReason", "darf nicht leer sein");
        }
        if (status != TravelRequestStatus.SUBMITTED) {
            throw new InvalidTravelRequestStateException(status, "reject");
        }
        this.status = TravelRequestStatus.REJECTED;
        this.approverId = approverId;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    /**
     * Business-Methode: Reiseabschnitt hinzufügen
     * Darf nur im Status DRAFT erfolgen
     */
    public TravelLeg addTravelLeg(String departureLocation, String arrivalLocation, 
                                  TransportationType transportationType, Money cost) {
        if (status != TravelRequestStatus.DRAFT) {
            throw new IllegalStateException(
                "Reiseabschnitte können nur im Entwurfsstatus hinzugefügt werden. Aktueller Status: " + status
            );
        }
        
        TravelLeg travelLeg = new TravelLeg(this, departureLocation, arrivalLocation, 
                                            transportationType, cost);
        this.travelLegs.add(travelLeg);
        return travelLeg;
    }
    
    /**
     * Business-Methode: Reiseabschnitt entfernen
     * Darf nur im Status DRAFT erfolgen
     */
    public void removeTravelLeg(TravelLeg travelLeg) {
        if (status != TravelRequestStatus.DRAFT) {
            throw new IllegalStateException(
                "Reiseabschnitte können nur im Entwurfsstatus entfernt werden. Aktueller Status: " + status
            );
        }
        if (travelLeg == null) {
            throw new IllegalArgumentException("TravelLeg darf nicht null sein");
        }
        
        // Bidirektionale Beziehung auflösen
        travelLeg.setTravelRequest(null);
        this.travelLegs.remove(travelLeg);
    }
    
    /**
     * Business-Methode: Reiseabschnitt anhand der ID entfernen
     * Darf nur im Status DRAFT erfolgen
     */
    public void removeTravelLegById(Long legId) {
        if (status != TravelRequestStatus.DRAFT) {
            throw new IllegalStateException(
                "Reiseabschnitte können nur im Entwurfsstatus entfernt werden. Aktueller Status: " + status
            );
        }
        
        TravelLeg legToRemove = this.travelLegs.stream()
            .filter(leg -> leg.getId() != null && leg.getId().equals(legId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("TravelLeg mit ID " + legId + " nicht gefunden"));
        
        // Bidirektionale Beziehung auflösen
        legToRemove.setTravelRequest(null);
        this.travelLegs.remove(legToRemove);
    }
    
    /**
     * Gibt eine unveränderliche Liste aller Reiseabschnitte zurück
     */
    public List<TravelLeg> getTravelLegs() {
        return Collections.unmodifiableList(travelLegs);
    }
    
    /**
     * Berechnet die Gesamtkosten aller Reiseabschnitte
     * Alle Währungen müssen identisch sein
     */
    public Money calculateTotalLegCost() {
        if (travelLegs.isEmpty()) {
            return estimatedCost;
        }
        
        Currency baseCurrency = travelLegs.get(0).getCost().getCurrency();
        Money total = new Money(java.math.BigDecimal.ZERO, baseCurrency);
        
        for (TravelLeg leg : travelLegs) {
            if (!leg.getCost().getCurrency().equals(baseCurrency)) {
                throw new IllegalStateException(
                    "Alle Reiseabschnitte müssen die gleiche Währung haben"
                );
            }
            total = new Money(
                total.getAmount().add(leg.getCost().getAmount()), 
                baseCurrency
            );
        }
        
        return total;
    }

    /**
     * Business-Methode: Prüft ob für das Reiseziel genug Vorlaufzeit für Visa-Beantragung vorhanden ist
     *
     * DDD: Business-Logik gehört in die Entity
     *
     * @param destination Reiseziel mit Visa-Anforderungen
     * @throws com.travelreimburse.domain.exception.InsufficientVisaProcessingTimeException
     *         wenn nicht genug Zeit vorhanden ist
     */
    public void validateVisaProcessingTime(TravelDestination destination) {
        if (destination == null) {
            return; // Keine Validierung nötig
        }

        long daysUntilTravel = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.now().toLocalDate(),
            this.travelPeriod.getStartDate()
        );

        if (!destination.hasEnoughPreparationTime(daysUntilTravel)) {
            Integer requiredDays = destination.getVisaRequirement().getProcessingDays();
            throw new com.travelreimburse.domain.exception.InsufficientVisaProcessingTimeException(
                destination.getCountryCode().getCode(),
                requiredDays != null ? requiredDays : 0,
                daysUntilTravel
            );
        }
    }

    /**
     * Business-Methode: Gibt an ob die Reise spezielle Vorbereitung (Visa/Impfung) benötigt
     *
     * @param destination Reiseziel (optional)
     * @return true wenn Vorbereitung nötig, false sonst
     */
    public boolean requiresSpecialPreparation(TravelDestination destination) {
        if (destination == null) {
            return false;
        }
        return destination.requiresPreparation();
    }

    // ===== GETTER METHODS =====
    public Long getId() {
        return id;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public CostCenter getCostCenter() {
        return costCenter;
    }

    public String getDestination() {
        return destination;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public DateRange getTravelPeriod() {
        return travelPeriod;
    }
    
    public Money getEstimatedCost() {
        return estimatedCost;
    }

    public TravelRequestStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Long getApproverId() {
        return approverId;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public RetentionPeriod getRetentionPeriod() {
        return retentionPeriod;
    }

    public LocalDate getArchivedAt() {
        return retentionPeriod != null ? retentionPeriod.getArchivedAt() : null;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    // ===== BUSINESS METHODS =====

    /**
     * Business-Methode: Status ändern mit Validierung und Event Publishing
     * DDD: Rich Domain Model - Business logic belongs in the entity
     */
    public void updateStatus(TravelRequestStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (!canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot transition from %s to %s for TravelRequest %d",
                    this.status, newStatus, this.id)
            );
        }

        TravelRequestStatus oldStatus = this.status;
        this.status = newStatus;

        registerEvent(new TravelRequestStatusChangedEvent(
            this.id,
            oldStatus,
            newStatus
        ));
    }

    /**
     * Business logic for valid status transitions
     * State machine implementation
     */
    private boolean canTransitionTo(TravelRequestStatus targetStatus) {
        if (this.status == targetStatus) {
            return false;
        }

        return switch (this.status) {
            case DRAFT -> targetStatus == TravelRequestStatus.SUBMITTED;
            case SUBMITTED -> targetStatus == TravelRequestStatus.APPROVED ||
                            targetStatus == TravelRequestStatus.REJECTED;
            case APPROVED -> targetStatus == TravelRequestStatus.PAID;
            case PAID -> targetStatus == TravelRequestStatus.ARCHIVED;
            case REJECTED, ARCHIVED -> false;
        };
    }

    /**
     * Business-Methode: Archiviert den Reiseantrag
     * Status: PAID → ARCHIVED
     *
     * Invarianten:
     *  - Status muss PAID sein
     */
    public void archive() {
        validateCanBeArchived();
        this.status = TravelRequestStatus.ARCHIVED;
        this.retentionPeriod = RetentionPeriod.standard();
    }

    /**
     * Business-Methode: Archiviert mit benutzerdefinierter Frist
     */
    public void archiveWithCustomRetention(int retentionYears) {
        validateCanBeArchived();
        this.status = TravelRequestStatus.ARCHIVED;
        this.retentionPeriod = RetentionPeriod.custom(LocalDate.now(), retentionYears);
    }

    /**
     * Business-Methode: Markiert Reiseantrag als bezahlt
     * Status: APPROVED → PAID
     */
    public void pay() {
        if (this.status != TravelRequestStatus.APPROVED) {
            throw new InvalidTravelRequestStateException(this.status, "pay");
        }
        this.status = TravelRequestStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * Prüft, ob Archivierung erlaubt ist
     */
    private void validateCanBeArchived() {
        if (this.status != TravelRequestStatus.PAID) {
            throw new CannotArchiveTravelRequestException(
                this.id,
                "Nur ausgezahlte Reisen können archiviert werden (aktueller Status: " + this.status + ")"
            );
        }

        if (this.status == TravelRequestStatus.ARCHIVED) {
            throw new CannotArchiveTravelRequestException(
                this.id,
                "Reise ist bereits archiviert"
            );
        }
    }

    /**
     * Prüft, ob die Aufbewahrungsfrist abgelaufen ist
     */
    public boolean canBeDeleted() {
        return this.status == TravelRequestStatus.ARCHIVED
            && this.retentionPeriod != null
            && this.retentionPeriod.isExpired();
    }

    /**
     * Business-Query: Ist archiviert?
     */
    public boolean isArchived() {
        return this.status == TravelRequestStatus.ARCHIVED;
    }

    /**
     * Business-Query: Wurde bezahlt?
     */
    public boolean isPaid() {
        return this.status == TravelRequestStatus.PAID;
    }
}

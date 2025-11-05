package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Aggregate Root für Reiseanträge
 * Verwaltet alle Informationen zu einer beantragten Dienstreise
 */
@Entity
@Table(name = "travel_requests")
public class TravelRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long employeeId;
    
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
    
    // JPA benötigt Default-Konstruktor
    protected TravelRequest() {
    }
    
    /**
     * Erstellt einen neuen Reiseantrag im Status DRAFT
     */
    public TravelRequest(Long employeeId, String destination, String purpose, 
                         DateRange travelPeriod, Money estimatedCost) {
        if (employeeId == null) {
            throw new IllegalArgumentException("EmployeeId darf nicht null sein");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Reiseziel darf nicht leer sein");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Reisezweck darf nicht leer sein");
        }
        if (travelPeriod == null) {
            throw new IllegalArgumentException("Reisezeitraum darf nicht null sein");
        }
        if (estimatedCost == null) {
            throw new IllegalArgumentException("Geschätzte Kosten dürfen nicht null sein");
        }
        
        this.employeeId = employeeId;
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
            throw new IllegalStateException(
                "Nur Entwürfe können eingereicht werden. Aktueller Status: " + status
            );
        }
        this.status = TravelRequestStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public Long getEmployeeId() {
        return employeeId;
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
}

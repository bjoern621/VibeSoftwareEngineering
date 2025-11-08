package com.travelreimburse.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity für einzelne Reiseabschnitte (Teil einer Reiseroute)
 * Managed von TravelRequest als Aggregate Root
 */
@Entity
@Table(name = "travel_legs")
public class TravelLeg {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "travel_request_id", nullable = false)
    private TravelRequest travelRequest;
    
    @Column(nullable = false, length = 200)
    private String departureLocation;
    
    @Column(nullable = false, length = 200)
    private String arrivalLocation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportationType transportationType;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "cost_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "cost_currency"))
    })
    private Money cost;
    
    @Column(length = 500)
    private String description;
    
    @Column
    private LocalDateTime departureDateTime;
    
    @Column
    private LocalDateTime arrivalDateTime;
    
    @Column
    private Double distanceKm;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // JPA benötigt Default-Konstruktor
    protected TravelLeg() {
    }
    
    /**
     * Erstellt einen neuen Reiseabschnitt
     */
    public TravelLeg(TravelRequest travelRequest, String departureLocation, String arrivalLocation, 
                     TransportationType transportationType, Money cost) {
        validateParameters(travelRequest, departureLocation, arrivalLocation, transportationType, cost);
        
        this.travelRequest = travelRequest;
        this.departureLocation = departureLocation;
        this.arrivalLocation = arrivalLocation;
        this.transportationType = transportationType;
        this.cost = cost;
        this.createdAt = LocalDateTime.now();
    }
    
    private void validateParameters(TravelRequest travelRequest, String departureLocation, 
                                   String arrivalLocation, TransportationType transportationType, Money cost) {
        if (travelRequest == null) {
            throw new IllegalArgumentException("TravelRequest darf nicht null sein");
        }
        if (departureLocation == null || departureLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Abfahrtsort darf nicht leer sein");
        }
        if (arrivalLocation == null || arrivalLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Ankunftsort darf nicht leer sein");
        }
        if (transportationType == null) {
            throw new IllegalArgumentException("Verkehrsmittel darf nicht null sein");
        }
        if (cost == null) {
            throw new IllegalArgumentException("Kosten dürfen nicht null sein");
        }
    }
    
    // Business-Methoden
    
    /**
     * Business-Methode: Aktualisiert die Beschreibung des Reiseabschnitts
     */
    public void updateDescription(String description) {
        this.description = description;
    }
    
    /**
     * Business-Methode: Aktualisiert die Abfahrtszeit
     * Validiert dass Abfahrtszeit vor Ankunftszeit liegt
     */
    public void updateDepartureDateTime(LocalDateTime departureDateTime) {
        if (arrivalDateTime != null && departureDateTime != null && 
            departureDateTime.isAfter(arrivalDateTime)) {
            throw new IllegalArgumentException("Abfahrtszeit muss vor Ankunftszeit liegen");
        }
        this.departureDateTime = departureDateTime;
    }
    
    /**
     * Business-Methode: Aktualisiert die Ankunftszeit
     * Validiert dass Ankunftszeit nach Abfahrtszeit liegt
     */
    public void updateArrivalDateTime(LocalDateTime arrivalDateTime) {
        if (departureDateTime != null && arrivalDateTime != null && 
            arrivalDateTime.isBefore(departureDateTime)) {
            throw new IllegalArgumentException("Ankunftszeit muss nach Abfahrtszeit liegen");
        }
        this.arrivalDateTime = arrivalDateTime;
    }
    
    /**
     * Business-Methode: Aktualisiert die Distanz in Kilometern
     * Validiert dass Distanz nicht negativ ist
     */
    public void updateDistanceKm(Double distanceKm) {
        if (distanceKm != null && distanceKm < 0) {
            throw new IllegalArgumentException("Distanz darf nicht negativ sein");
        }
        this.distanceKm = distanceKm;
    }
    
    /**
     * Business-Methode: Aktualisiert die Kosten des Reiseabschnitts
     */
    public void updateCost(Money newCost) {
        if (newCost == null) {
            throw new IllegalArgumentException("Neue Kosten dürfen nicht null sein");
        }
        this.cost = newCost;
    }
    
    /**
     * Setzt die Beziehung zum TravelRequest (nur für Entfernung benötigt)
     * Package-private, da nur von TravelRequest.removeTravelLeg() verwendet
     */
    void setTravelRequest(TravelRequest travelRequest) {
        this.travelRequest = travelRequest;
    }
    
    // Getter
    
    public Long getId() {
        return id;
    }
    
    public TravelRequest getTravelRequest() {
        return travelRequest;
    }
    
    public String getDepartureLocation() {
        return departureLocation;
    }
    
    public String getArrivalLocation() {
        return arrivalLocation;
    }
    
    public TransportationType getTransportationType() {
        return transportationType;
    }
    
    public Money getCost() {
        return cost;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }
    
    public LocalDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

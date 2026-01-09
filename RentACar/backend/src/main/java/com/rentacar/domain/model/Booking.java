package com.rentacar.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Booking Aggregate Root.
 * 
 * Repräsentiert eine Buchung (Reservierung) eines Fahrzeugs durch einen Kunden.
 * Enthält Business-Logik für Statusübergänge und Validierung.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final int DEFAULT_INCLUDED_KILOMETERS = 500;
    private static final int CANCELLATION_DEADLINE_HOURS = 24;
    private static final int STATUS_COLUMN_LENGTH = 20;
    private static final int CURRENCY_COLUMN_LENGTH = 3;
    private static final int CANCELLATION_REASON_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_branch_id", nullable = false)
    private Branch pickupBranch;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "return_branch_id", nullable = false)
    private Branch returnBranch;

    @Column(name = "pickup_date_time", nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(name = "return_date_time", nullable = false)
    private LocalDateTime returnDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = STATUS_COLUMN_LENGTH)
    private BookingStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = CURRENCY_COLUMN_LENGTH)
    private String currency = DEFAULT_CURRENCY;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "booking_additional_services", joinColumns = @JoinColumn(name = "booking_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private Set<AdditionalServiceType> additionalServices = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancellation_reason", length = CANCELLATION_REASON_LENGTH)
    private String cancellationReason;

    @Column(name = "included_kilometers")
    private Integer includedKilometers = DEFAULT_INCLUDED_KILOMETERS;

    /**
     * JPA-Konstruktor.
     */
    protected Booking() {
        // Für JPA
    }

    /**
     * Erstellt eine neue Buchung.
     * 
     * @param customer Kunde, der die Buchung durchführt
     * @param vehicle Gebuchtes Fahrzeug
     * @param pickupBranch Abholfiliale
     * @param returnBranch Rückgabefiliale
     * @param pickupDateTime Abholdatum und -zeit
     * @param returnDateTime Rückgabedatum und -zeit
     * @param totalPrice Gesamtpreis
     * @param additionalServices Zusatzleistungen (optional)
     */
    public Booking(Customer customer, Vehicle vehicle, Branch pickupBranch, 
                   Branch returnBranch, LocalDateTime pickupDateTime, 
                   LocalDateTime returnDateTime, BigDecimal totalPrice,
                   Set<AdditionalServiceType> additionalServices) {
        this(customer, vehicle, pickupBranch, returnBranch, pickupDateTime, returnDateTime, totalPrice, additionalServices, DEFAULT_INCLUDED_KILOMETERS);
    }

    /**
     * Erstellt eine neue Buchung mit vereinbarten Freikilometern.
     * 
     * @param customer Kunde, der die Buchung durchführt
     * @param vehicle Gebuchtes Fahrzeug
     * @param pickupBranch Abholfiliale
     * @param returnBranch Rückgabefiliale
     * @param pickupDateTime Abholdatum und -zeit
     * @param returnDateTime Rückgabedatum und -zeit
     * @param totalPrice Gesamtpreis
     * @param additionalServices Zusatzleistungen (optional)
     * @param includedKilometers Vereinbarte Freikilometer
     */
    public Booking(Customer customer, Vehicle vehicle, Branch pickupBranch, 
                   Branch returnBranch, LocalDateTime pickupDateTime, 
                   LocalDateTime returnDateTime, BigDecimal totalPrice,
                   Set<AdditionalServiceType> additionalServices,
                   Integer includedKilometers) {
        validateBookingData(customer, vehicle, pickupBranch, returnBranch, 
                           pickupDateTime, returnDateTime, totalPrice);
        
        this.customer = customer;
        this.vehicle = vehicle;
        this.pickupBranch = pickupBranch;
        this.returnBranch = returnBranch;
        this.pickupDateTime = pickupDateTime;
        this.returnDateTime = returnDateTime;
        this.totalPrice = totalPrice;
        if (additionalServices != null) {
            this.additionalServices.addAll(additionalServices);
        }
        this.includedKilometers = includedKilometers != null ? includedKilometers : DEFAULT_INCLUDED_KILOMETERS;
        this.status = BookingStatus.REQUESTED;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Bestätigt die Buchung.
     * Nur REQUESTED Buchungen können bestätigt werden.
     * 
     * @throws com.rentacar.domain.exception.BookingStatusTransitionException wenn Status-Übergang ungültig
     */
    public void confirm() {
        if (status != BookingStatus.REQUESTED) {
            throw new com.rentacar.domain.exception.BookingStatusTransitionException(
                id, status, BookingStatus.CONFIRMED,
                "Nur Buchungen im Status REQUESTED können bestätigt werden"
            );
        }
        this.status = BookingStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Storniert die Buchung.
     * 
     * @param now Aktuelle Zeit
     * @param reason Stornierungsgrund
     * @throws com.rentacar.domain.exception.CancellationDeadlineExceededException wenn Stornierungsfrist überschritten
     * @throws com.rentacar.domain.exception.BookingStatusTransitionException wenn Status nicht stornierbar
     */
    public void cancel(LocalDateTime now, String reason) {
        validateCancellationWindow(now);
        
        if (!status.isCancellable()) {
            throw new com.rentacar.domain.exception.BookingStatusTransitionException(
                id, status, BookingStatus.CANCELLED,
                "Buchung im Status " + status + " kann nicht storniert werden"
            );
        }
        
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Markiert die Buchung als abgelaufen.
     * Wird verwendet, wenn Kunde das Fahrzeug nicht zur vereinbarten Zeit abholt.
     * 
     * @throws com.rentacar.domain.exception.BookingStatusTransitionException wenn Status-Übergang ungültig
     */
    public void expire() {
        if (status != BookingStatus.REQUESTED && status != BookingStatus.CONFIRMED) {
            throw new com.rentacar.domain.exception.BookingStatusTransitionException(
                id, status, BookingStatus.EXPIRED,
                "Nur REQUESTED oder CONFIRMED Buchungen können ablaufen"
            );
        }
        this.status = BookingStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Aktiviert die Buchung (Fahrzeug abgeholt).
     * 
     * @throws com.rentacar.domain.exception.BookingStatusTransitionException wenn Status-Übergang ungültig
     */
    public void activate() {
        if (status != BookingStatus.CONFIRMED) {
            throw new com.rentacar.domain.exception.BookingStatusTransitionException(
                id, status, BookingStatus.ACTIVE,
                "Nur bestätigte Buchungen können aktiviert werden"
            );
        }
        this.status = BookingStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Markiert die Buchung als abgeschlossen (Fahrzeug zurückgegeben).
     * 
     * @throws com.rentacar.domain.exception.BookingStatusTransitionException wenn Status-Übergang ungültig
     */
    public void complete() {
        if (status != BookingStatus.ACTIVE) {
            throw new com.rentacar.domain.exception.BookingStatusTransitionException(
                id, status, BookingStatus.COMPLETED,
                "Nur aktive Buchungen können abgeschlossen werden"
            );
        }
        this.status = BookingStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validiert, ob die Stornierungsfrist eingehalten wird (24h vor Abholung).
     * 
     * @param now Aktuelle Zeit
     * @throws com.rentacar.domain.exception.CancellationDeadlineExceededException wenn Frist überschritten
     */
    private void validateCancellationWindow(LocalDateTime now) {
        LocalDateTime cancellationDeadline = pickupDateTime.minusHours(CANCELLATION_DEADLINE_HOURS);
        
        if (now.isAfter(cancellationDeadline)) {
            throw new com.rentacar.domain.exception.CancellationDeadlineExceededException(
                id, pickupDateTime,
                "Stornierung muss mindestens " + CANCELLATION_DEADLINE_HOURS + " Stunden vor Abholung erfolgen"
            );
        }
    }

    /**
     * Validiert die Buchungsdaten bei Erstellung.
     * 
     * @throws com.rentacar.domain.exception.InvalidBookingDataException wenn Validierung fehlschlägt
     */
    private void validateBookingData(Customer customer, Vehicle vehicle, Branch pickupBranch,
                                     Branch returnBranch, LocalDateTime pickupDateTime,
                                     LocalDateTime returnDateTime, BigDecimal totalPrice) {
        Objects.requireNonNull(customer, "Kunde darf nicht null sein");
        Objects.requireNonNull(vehicle, "Fahrzeug darf nicht null sein");
        Objects.requireNonNull(pickupBranch, "Abholfiliale darf nicht null sein");
        Objects.requireNonNull(returnBranch, "Rückgabefiliale darf nicht null sein");
        Objects.requireNonNull(pickupDateTime, "Abholdatum darf nicht null sein");
        Objects.requireNonNull(returnDateTime, "Rückgabedatum darf nicht null sein");
        Objects.requireNonNull(totalPrice, "Gesamtpreis darf nicht null sein");
        
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.rentacar.domain.exception.InvalidBookingDataException(
                "totalPrice", "Gesamtpreis darf nicht negativ sein");
        }
        
        if (!pickupDateTime.isBefore(returnDateTime)) {
            throw new com.rentacar.domain.exception.InvalidBookingDataException(
                "Abholdatum muss vor Rückgabedatum liegen");
        }
    }

    // Getter (keine Setter für kritische Felder - DDD Prinzip!)
    
    public Long getId() { 
        return id; 
    }
    
    public Customer getCustomer() { 
        return customer; 
    }
    
    public Vehicle getVehicle() { 
        return vehicle; 
    }
    
    public Branch getPickupBranch() { 
        return pickupBranch; 
    }
    
    public Branch getReturnBranch() { 
        return returnBranch; 
    }
    
    public LocalDateTime getPickupDateTime() { 
        return pickupDateTime; 
    }
    
    public LocalDateTime getReturnDateTime() { 
        return returnDateTime; 
    }
    
    public BookingStatus getStatus() { 
        return status; 
    }
    
    public BigDecimal getTotalPrice() { 
        return totalPrice; 
    }
    
    public String getCurrency() { 
        return currency; 
    }
    
    public Set<AdditionalServiceType> getAdditionalServices() { 
        return Collections.unmodifiableSet(additionalServices); 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public String getCancellationReason() { 
        return cancellationReason; 
    }

    public Integer getIncludedKilometers() {
        return includedKilometers;
    }
}

package com.rentacar.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root für den Mietvertrag.
 * Repräsentiert die tatsächliche Durchführung der Vermietung.
 */
@Entity
@Table(name = "rental_agreements")
public class RentalAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true, nullable = false)
    private Booking booking;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "kilometers", column = @Column(name = "checkout_kilometers", nullable = false))
    })
    private Mileage checkoutMileage;

    @Column(name = "checkout_time", nullable = false)
    private LocalDateTime checkoutTime;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fuelLevel", column = @Column(name = "checkout_fuel_level")),
        @AttributeOverride(name = "cleanliness", column = @Column(name = "checkout_cleanliness")),
        @AttributeOverride(name = "damagesDescription", column = @Column(name = "checkout_damages"))
    })
    private VehicleCondition checkoutCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalAgreementStatus status;

    protected RentalAgreement() {
        // JPA
    }

    public RentalAgreement(Booking booking, Mileage checkoutMileage, LocalDateTime checkoutTime, VehicleCondition checkoutCondition) {
        this.booking = Objects.requireNonNull(booking, "Booking must not be null");
        this.checkoutMileage = Objects.requireNonNull(checkoutMileage, "Checkout mileage must not be null");
        this.checkoutTime = Objects.requireNonNull(checkoutTime, "Checkout time must not be null");
        this.checkoutCondition = Objects.requireNonNull(checkoutCondition, "Checkout condition must not be null");
        this.status = RentalAgreementStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public Mileage getCheckoutMileage() {
        return checkoutMileage;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public VehicleCondition getCheckoutCondition() {
        return checkoutCondition;
    }

    public RentalAgreementStatus getStatus() {
        return status;
    }
}

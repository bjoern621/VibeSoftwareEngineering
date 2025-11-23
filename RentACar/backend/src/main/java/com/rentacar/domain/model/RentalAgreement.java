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

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "kilometers", column = @Column(name = "checkin_kilometers")),
    })
    private Mileage checkinMileage;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fuelLevel", column = @Column(name = "checkin_fuel_level")),
        @AttributeOverride(name = "cleanliness", column = @Column(name = "checkin_cleanliness")),
        @AttributeOverride(name = "damagesDescription", column = @Column(name = "checkin_damages"))
    })
    private VehicleCondition checkinCondition;

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

    public void checkIn(Mileage checkinMileage, LocalDateTime checkinTime, VehicleCondition checkinCondition) {
        if (this.status != RentalAgreementStatus.OPEN) {
            throw new IllegalStateException("Rental agreement is not open. Current status: " + this.status);
        }
        this.checkinMileage = Objects.requireNonNull(checkinMileage, "Checkin mileage must not be null");
        this.checkinTime = Objects.requireNonNull(checkinTime, "Checkin time must not be null");
        this.checkinCondition = Objects.requireNonNull(checkinCondition, "Checkin condition must not be null");
        
        if (checkinMileage.isLessThan(this.checkoutMileage)) {
             throw new IllegalArgumentException("Checkin mileage cannot be less than checkout mileage");
        }

        if (checkinTime.isBefore(this.checkoutTime)) {
            throw new IllegalArgumentException("Checkin time cannot be before checkout time");
        }

        this.status = RentalAgreementStatus.CLOSED;
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

    public Mileage getCheckinMileage() {
        return checkinMileage;
    }

    public LocalDateTime getCheckinTime() {
        return checkinTime;
    }

    public VehicleCondition getCheckinCondition() {
        return checkinCondition;
    }

    public RentalAgreementStatus getStatus() {
        return status;
    }
}

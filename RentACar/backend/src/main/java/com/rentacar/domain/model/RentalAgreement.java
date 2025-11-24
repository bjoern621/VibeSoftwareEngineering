package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidMileageException;
import com.rentacar.domain.exception.InvalidRentalAgreementDataException;
import com.rentacar.domain.exception.RentalAgreementStatusTransitionException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Embedded
    private AdditionalCosts additionalCosts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalAgreementStatus status;

    protected RentalAgreement() {
        // JPA
    }

    public RentalAgreement(Booking booking, Mileage checkoutMileage, LocalDateTime checkoutTime, VehicleCondition checkoutCondition) {
        if (booking == null) {
            throw new InvalidRentalAgreementDataException("Booking must not be null");
        }
        if (checkoutMileage == null) {
            throw new InvalidRentalAgreementDataException("Checkout mileage must not be null");
        }
        if (checkoutTime == null) {
            throw new InvalidRentalAgreementDataException("Checkout time must not be null");
        }
        if (checkoutCondition == null) {
            throw new InvalidRentalAgreementDataException("Checkout condition must not be null");
        }
        
        this.booking = booking;
        this.checkoutMileage = checkoutMileage;
        this.checkoutTime = checkoutTime;
        this.checkoutCondition = checkoutCondition;
        this.status = RentalAgreementStatus.OPEN;
        this.additionalCosts = AdditionalCosts.zero();
    }

    public void checkIn(Mileage checkinMileage, LocalDateTime checkinTime, VehicleCondition checkinCondition) {
        if (this.status != RentalAgreementStatus.OPEN) {
            throw new RentalAgreementStatusTransitionException(this.id, this.status, RentalAgreementStatus.CLOSED);
        }
        
        if (checkinMileage == null) {
            throw new InvalidRentalAgreementDataException("Checkin mileage must not be null");
        }
        if (checkinTime == null) {
            throw new InvalidRentalAgreementDataException("Checkin time must not be null");
        }
        if (checkinCondition == null) {
            throw new InvalidRentalAgreementDataException("Checkin condition must not be null");
        }
        
        this.checkinMileage = checkinMileage;
        this.checkinTime = checkinTime;
        this.checkinCondition = checkinCondition;
        
        if (checkinMileage.isLessThan(this.checkoutMileage)) {
             throw new InvalidMileageException("Rückgabe-Kilometerstand darf nicht kleiner als Ausgabe-Kilometerstand sein");
        }

        if (checkinTime.isBefore(this.checkoutTime)) {
            throw new InvalidRentalAgreementDataException("Rückgabezeitpunkt darf nicht vor Ausgabezeitpunkt liegen");
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

    public AdditionalCosts getAdditionalCosts() {
        return additionalCosts;
    }

    public void updateAdditionalCosts(AdditionalCosts additionalCosts) {
        if (additionalCosts == null) {
            throw new InvalidRentalAgreementDataException("Additional costs must not be null");
        }
        this.additionalCosts = additionalCosts;
    }

    public RentalAgreementStatus getStatus() {
        return status;
    }
}

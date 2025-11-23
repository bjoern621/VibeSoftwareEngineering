package com.rentacar.application.service;

import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BookingRepository;
import com.rentacar.domain.repository.RentalAgreementRepository;
import com.rentacar.domain.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RentalApplicationService {

    private final BookingRepository bookingRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final VehicleRepository vehicleRepository;

    public RentalApplicationService(BookingRepository bookingRepository, 
                                    RentalAgreementRepository rentalAgreementRepository,
                                    VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Long performCheckOut(Long bookingId, Integer mileage, String fuelLevel, String cleanliness, String damagesDescription) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        // 1. Validate Booking Status
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking must be CONFIRMED to perform checkout. Current status: " + booking.getStatus());
        }

        Vehicle vehicle = booking.getVehicle();
        
        // 2. Update Vehicle Status
        vehicle.markAsRented();
        vehicleRepository.save(vehicle); // Explicit save, though transactional should handle it

        // 3. Create RentalAgreement
        RentalAgreement agreement = new RentalAgreement(
                booking,
                Mileage.of(mileage),
                LocalDateTime.now(),
                new VehicleCondition(fuelLevel, cleanliness, damagesDescription)
        );
        
        rentalAgreementRepository.save(agreement);
        
        // 4. Update Booking Status
        booking.activate();
        bookingRepository.save(booking);
        
        return agreement.getId();
    }
}

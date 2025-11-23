package com.rentacar.application.service;

import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BookingRepository;
import com.rentacar.domain.repository.DamageReportRepository;
import com.rentacar.domain.repository.RentalAgreementRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.domain.service.AdditionalCostService;
import com.rentacar.domain.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RentalApplicationService {

    private final BookingRepository bookingRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final VehicleRepository vehicleRepository;
    private final DamageReportRepository damageReportRepository;
    private final EmailService emailService;
    private final AdditionalCostService additionalCostService;

    public RentalApplicationService(BookingRepository bookingRepository, 
                                    RentalAgreementRepository rentalAgreementRepository,
                                    VehicleRepository vehicleRepository,
                                    DamageReportRepository damageReportRepository,
                                    EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.vehicleRepository = vehicleRepository;
        this.damageReportRepository = damageReportRepository;
        this.emailService = emailService;
        this.additionalCostService = new AdditionalCostService();
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

    @Transactional
    public void performCheckIn(Long bookingId, Integer mileage, String fuelLevel, String cleanliness, String damagesDescription) {
        RentalAgreement agreement = rentalAgreementRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Rental agreement not found for booking ID: " + bookingId));

        Booking booking = agreement.getBooking();
        Vehicle vehicle = booking.getVehicle();

        // 1. Perform Check-in on Agreement
        agreement.checkIn(
                Mileage.of(mileage),
                LocalDateTime.now(),
                new VehicleCondition(fuelLevel, cleanliness, damagesDescription)
        );

        // 2. Calculate Additional Costs
        List<DamageReport> damageReports = damageReportRepository.findByRentalAgreementId(agreement.getId());
        AdditionalCosts costs = additionalCostService.calculateAdditionalCosts(booking, agreement, damageReports);
        agreement.updateAdditionalCosts(costs);

        rentalAgreementRepository.save(agreement);

        // 3. Update Vehicle Status
        vehicle.markAsAvailable(Mileage.of(mileage));
        
        if (damagesDescription != null && !damagesDescription.isBlank()) {
            vehicle.markAsInMaintenance();
        }
        vehicleRepository.save(vehicle);

        // 4. Complete Booking
        booking.complete();
        bookingRepository.save(booking);

        // 5. Send Invoice Email
        Customer customer = booking.getCustomer();
        emailService.sendInvoiceEmail(customer.getEmail(), customer.getFirstName() + " " + customer.getLastName(), booking, agreement);
    }
}

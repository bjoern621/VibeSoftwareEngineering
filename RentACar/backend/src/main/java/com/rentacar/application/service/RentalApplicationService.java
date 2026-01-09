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
        System.out.println("[DEBUG] performCheckOut called - bookingId: " + bookingId + ", mileage: " + mileage + ", fuelLevel: " + fuelLevel + ", cleanliness: " + cleanliness);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
        
        System.out.println("[DEBUG] Booking found - ID: " + booking.getId() + ", Status: " + booking.getStatus());

        // 1. Validate Booking Status
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking must be CONFIRMED to perform checkout. Current status: " + booking.getStatus());
        }

        Vehicle vehicle = booking.getVehicle();
        Mileage checkoutMileage = Mileage.of(mileage);
        
        // 2. Validate Mileage - must be >= current vehicle mileage
        if (checkoutMileage.isLessThan(vehicle.getMileage())) {
            throw new IllegalArgumentException(
                "Checkout-Kilometerstand (" + checkoutMileage.getKilometers() + 
                ") darf nicht kleiner sein als aktueller Fahrzeugstand (" + vehicle.getMileage().getKilometers() + ")"
            );
        }
        
        // 3. Update Vehicle Status
        vehicle.markAsRented();
        vehicleRepository.save(vehicle); // Explicit save, though transactional should handle it

        // 4. Create RentalAgreement
        System.out.println("[DEBUG] Creating VehicleCondition - fuelLevel: " + fuelLevel + ", cleanliness: " + cleanliness + ", damagesDescription: " + damagesDescription);
        VehicleCondition condition = new VehicleCondition(fuelLevel, cleanliness, damagesDescription);
        System.out.println("[DEBUG] VehicleCondition created successfully");
        
        RentalAgreement agreement = new RentalAgreement(
                booking,
                checkoutMileage,
                LocalDateTime.now(),
                condition
        );
        System.out.println("[DEBUG] RentalAgreement created successfully - ID will be assigned after save");
        
        rentalAgreementRepository.save(agreement);
        
        // 5. Update Booking Status
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

        // 2. Create DamageReport if damages are reported
        if (damagesDescription != null && !damagesDescription.isBlank()) {
            DamageReport damageReport = new DamageReport(agreement, damagesDescription, null, null);
            damageReportRepository.save(damageReport);
        }

        // 3. Calculate Additional Costs
        List<DamageReport> damageReports = damageReportRepository.findByRentalAgreementId(agreement.getId());
        AdditionalCosts costs = additionalCostService.calculateAdditionalCosts(booking, agreement, damageReports);
        agreement.updateAdditionalCosts(costs);

        rentalAgreementRepository.save(agreement);

        // 4. Update Vehicle Status
        vehicle.markAsAvailable(Mileage.of(mileage));
        
        if (damagesDescription != null && !damagesDescription.isBlank()) {
            vehicle.markAsInMaintenance();
        }
        vehicleRepository.save(vehicle);

        // 5. Complete Booking
        booking.complete();
        bookingRepository.save(booking);

        // 6. Send Invoice Email
        Customer customer = booking.getCustomer();
        emailService.sendInvoiceEmail(customer.getEmail(), customer.getFirstName() + " " + customer.getLastName(), booking, agreement);
    }
}

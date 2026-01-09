package com.rentacar.domain.service;

import com.rentacar.domain.exception.VehicleNotAvailableException;
import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BookingRepository;
import com.rentacar.domain.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class BookingDomainService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final PricingService pricingService;

    public BookingDomainService(BookingRepository bookingRepository, 
                                VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.pricingService = new PricingService();
    }

    public Booking createBooking(Customer customer, Vehicle vehicle, Branch pickupBranch, 
                                 Branch returnBranch, LocalDateTime pickupDateTime, 
                                 LocalDateTime returnDateTime, Set<AdditionalServiceType> additionalServices) {
        
        // 1. Check Vehicle Status
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            List<Vehicle> alternatives = vehicleRepository.findAvailableVehicles(
                vehicle.getVehicleType(), pickupDateTime, returnDateTime);
            throw new VehicleNotAvailableException("Fahrzeug ist nicht verfügbar (Status: " + vehicle.getStatus() + ")", alternatives);
        }

        // 2. Check Overlaps (Pessimistic Lock)
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(vehicle.getId(), pickupDateTime, returnDateTime);
        if (!overlaps.isEmpty()) {
            List<Vehicle> alternatives = vehicleRepository.findAvailableVehicles(
                vehicle.getVehicleType(), pickupDateTime, returnDateTime);
            throw new VehicleNotAvailableException("Fahrzeug ist im gewählten Zeitraum bereits gebucht.", alternatives);
        }

        // 3. Calculate Price
        DateRange rentalPeriod = new DateRange(pickupDateTime, returnDateTime);
        PricingCalculation calculation = pricingService.calculatePrice(vehicle.getVehicleType(), rentalPeriod, new java.util.ArrayList<>(additionalServices));

        // 4. Create Booking
        Booking booking = new Booking(customer, vehicle, pickupBranch, returnBranch, 
                                      pickupDateTime, returnDateTime, calculation.getTotalPrice(), additionalServices);
        
        return bookingRepository.save(booking);
    }
}

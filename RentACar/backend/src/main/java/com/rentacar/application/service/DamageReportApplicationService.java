package com.rentacar.application.service;

import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.DamageReportRepository;
import com.rentacar.domain.repository.RentalAgreementRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.domain.service.EmailService;
import com.rentacar.presentation.dto.CreateDamageReportRequestDTO;
import com.rentacar.presentation.dto.DamageReportResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DamageReportApplicationService {

    private final DamageReportRepository damageReportRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;

    public DamageReportApplicationService(DamageReportRepository damageReportRepository,
                                          RentalAgreementRepository rentalAgreementRepository,
                                          VehicleRepository vehicleRepository,
                                          EmailService emailService) {
        this.damageReportRepository = damageReportRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.vehicleRepository = vehicleRepository;
        this.emailService = emailService;
    }

    @Transactional
    public DamageReportResponseDTO createDamageReport(Long bookingId, CreateDamageReportRequestDTO request) {
        RentalAgreement rentalAgreement = rentalAgreementRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Rental agreement not found for booking ID: " + bookingId));

        DamageReport damageReport = new DamageReport(
                rentalAgreement,
                request.getDescription(),
                request.getEstimatedCost(),
                request.getPhotos()
        );

        damageReportRepository.save(damageReport);

        // Update Vehicle Status
        Vehicle vehicle = rentalAgreement.getBooking().getVehicle();
        // Note: This might throw VehicleStatusTransitionException if vehicle is RENTED.
        // We assume damage reports are typically created after check-in or require the vehicle to be not RENTED.
        // If the vehicle is already IN_MAINTENANCE, this is fine (idempotent-ish logic in Vehicle needed or catch exception?)
        // Vehicle.markAsInMaintenance() throws if RENTED. It does NOT throw if already IN_MAINTENANCE (it just sets it).
        // So we are good unless it is RENTED.
        vehicle.markAsInMaintenance();
        vehicleRepository.save(vehicle);

        // Send Notification
        Customer customer = rentalAgreement.getBooking().getCustomer();
        emailService.sendDamageReportNotification(customer.getEmail(), customer.getFirstName() + " " + customer.getLastName(), damageReport.getDescription());

        return mapToDTO(damageReport);
    }

    public DamageReportResponseDTO getDamageReport(Long id) {
        DamageReport report = damageReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Damage report not found with ID: " + id));
        return mapToDTO(report);
    }

    private DamageReportResponseDTO mapToDTO(DamageReport report) {
        return new DamageReportResponseDTO(
                report.getId(),
                report.getRentalAgreement().getId(),
                report.getRentalAgreement().getBooking().getId(),
                report.getRentalAgreement().getBooking().getVehicle().getId(),
                report.getDescription(),
                report.getEstimatedCost(),
                report.getPhotos()
        );
    }
}

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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<DamageReportResponseDTO> getDamageReportsByBooking(Long bookingId) {
        RentalAgreement rentalAgreement = rentalAgreementRepository.findByBookingId(bookingId)
                .orElse(null);
        
        if (rentalAgreement == null) {
            return Collections.emptyList();
        }
        
        List<DamageReport> reports = damageReportRepository.findByRentalAgreementId(rentalAgreement.getId());
        return reports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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

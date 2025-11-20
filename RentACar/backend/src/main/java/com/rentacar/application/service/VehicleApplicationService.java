package com.rentacar.application.service;

import com.rentacar.domain.exception.BranchNotFoundException;
import com.rentacar.domain.exception.DuplicateLicensePlateException;
import com.rentacar.domain.exception.VehicleNotFoundException;
import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BranchRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.presentation.dto.CreateVehicleRequestDTO;
import com.rentacar.presentation.dto.UpdateVehicleRequestDTO;
import com.rentacar.presentation.dto.VehicleResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service für Fahrzeugverwaltung.
 * 
 * Orchestriert Use Cases und koordiniert zwischen Presentation Layer und Domain Layer.
 * Enthält keine Business-Logik - diese liegt in den Domain Entities und Services.
 */
@Service
@Transactional
public class VehicleApplicationService {
    
    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    
    public VehicleApplicationService(VehicleRepository vehicleRepository,
                                    BranchRepository branchRepository) {
        this.vehicleRepository = vehicleRepository;
        this.branchRepository = branchRepository;
    }
    
    /**
     * Erstellt ein neues Fahrzeug.
     * 
     * @param request die Fahrzeugdaten
     * @return das erstellte Fahrzeug als Response DTO
     * @throws DuplicateLicensePlateException wenn das Kennzeichen bereits existiert
     * @throws BranchNotFoundException wenn die Filiale nicht existiert
     */
    public VehicleResponseDTO createVehicle(CreateVehicleRequestDTO request) {
        // Validierung: Kennzeichen muss eindeutig sein
        LicensePlate licensePlate = LicensePlate.of(request.getLicensePlate());
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new DuplicateLicensePlateException(request.getLicensePlate());
        }
        
        // Filiale muss existieren
        Branch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> new BranchNotFoundException(request.getBranchId()));
        
        // Domain Entity erstellen (Business-Logik in Entity)
        Vehicle vehicle = new Vehicle(
            licensePlate,
            request.getBrand(),
            request.getModel(),
            request.getYear(),
            Mileage.of(request.getMileage()),
            request.getVehicleType(),
            branch
        );
        
        // Persistieren
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        
        // DTO zurückgeben
        return VehicleResponseDTO.fromEntity(savedVehicle);
    }
    
    /**
     * Aktualisiert ein bestehendes Fahrzeug.
     * 
     * @param id die ID des Fahrzeugs
     * @param request die neuen Fahrzeugdaten
     * @return das aktualisierte Fahrzeug als Response DTO
     * @throws VehicleNotFoundException wenn das Fahrzeug nicht existiert
     * @throws BranchNotFoundException wenn die Filiale nicht existiert
     */
    public VehicleResponseDTO updateVehicle(Long id, UpdateVehicleRequestDTO request) {
        // Fahrzeug muss existieren
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException(id));
        
        // Filiale muss existieren
        Branch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> new BranchNotFoundException(request.getBranchId()));
        
        // Kilometerstand aktualisieren
        vehicle.updateMileage(Mileage.of(request.getMileage()));
        
        // Marke und Modell aktualisieren
        vehicle.updateBrandAndModel(request.getBrand(), request.getModel());
        
        // Filiale versetzen (falls unterschiedlich)
        if (!vehicle.getBranch().getId().equals(branch.getId())) {
            vehicle.relocateToBranch(branch);
        }
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        
        return VehicleResponseDTO.fromEntity(updatedVehicle);
    }
    
    /**
     * Markiert ein Fahrzeug als außer Betrieb.
     * 
     * @param id die ID des Fahrzeugs
     * @throws VehicleNotFoundException wenn das Fahrzeug nicht existiert
     */
    public void markVehicleAsOutOfService(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException(id));
        
        // Business-Methode der Entity nutzen
        vehicle.retire();
        
        vehicleRepository.save(vehicle);
    }
    
    /**
     * Gibt alle Fahrzeuge zurück.
     * 
     * @return Liste aller Fahrzeuge als Response DTOs
     */
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
            .map(VehicleResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Gibt ein einzelnes Fahrzeug zurück.
     * 
     * @param id die ID des Fahrzeugs
     * @return das Fahrzeug als Response DTO
     * @throws VehicleNotFoundException wenn das Fahrzeug nicht existiert
     */
    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException(id));
        
        return VehicleResponseDTO.fromEntity(vehicle);
    }
}

package com.rentacar.presentation.controller;

import com.rentacar.application.service.VehicleApplicationService;
import com.rentacar.presentation.dto.CreateVehicleRequestDTO;
import com.rentacar.presentation.dto.UpdateVehicleRequestDTO;
import com.rentacar.presentation.dto.VehicleResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller für Fahrzeugverwaltung.
 * 
 * Stellt die API Endpoints für CRUD-Operationen auf Fahrzeugen bereit.
 * Zugriff nur für Benutzer mit Rolle EMPLOYEE oder ADMIN.
 */
@RestController
@RequestMapping("/api/fahrzeuge")
public class VehicleController {
    
    private final VehicleApplicationService vehicleApplicationService;
    
    public VehicleController(VehicleApplicationService vehicleApplicationService) {
        this.vehicleApplicationService = vehicleApplicationService;
    }
    
    /**
     * POST /api/fahrzeuge - Erstellt ein neues Fahrzeug.
     * 
     * @param request die Fahrzeugdaten
     * @return das erstellte Fahrzeug mit Status 201 Created
     */
    @PostMapping
    public ResponseEntity<VehicleResponseDTO> createVehicle(@Valid @RequestBody CreateVehicleRequestDTO request) {
        VehicleResponseDTO response = vehicleApplicationService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/fahrzeuge/{id} - Aktualisiert ein bestehendes Fahrzeug.
     * 
     * @param id die ID des Fahrzeugs
     * @param request die neuen Fahrzeugdaten
     * @return das aktualisierte Fahrzeug mit Status 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequestDTO request) {
        VehicleResponseDTO response = vehicleApplicationService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PATCH /api/fahrzeuge/{id}/ausser-betrieb - Markiert ein Fahrzeug als außer Betrieb.
     * 
     * @param id die ID des Fahrzeugs
     * @return Status 204 No Content
     */
    @PatchMapping("/{id}/ausser-betrieb")
    public ResponseEntity<Void> markAsOutOfService(@PathVariable Long id) {
        vehicleApplicationService.markVehicleAsOutOfService(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * PATCH /api/fahrzeuge/{id}/vermieten - Markiert ein Fahrzeug als vermietet.
     * 
     * @param id die ID des Fahrzeugs
     * @return Status 204 No Content
     */
    @PatchMapping("/{id}/vermieten")
    public ResponseEntity<Void> markAsRented(@PathVariable Long id) {
        vehicleApplicationService.markVehicleAsRented(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * PATCH /api/fahrzeuge/{id}/zurueckgeben - Gibt ein Fahrzeug zurück.
     * 
     * @param id die ID des Fahrzeugs
     * @param returnMileage der Kilometerstand bei Rückgabe
     * @return Status 204 No Content
     */
    @PatchMapping("/{id}/zurueckgeben")
    public ResponseEntity<Void> returnVehicle(
            @PathVariable Long id,
            @RequestParam Integer returnMileage) {
        vehicleApplicationService.returnVehicle(id, returnMileage);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * PATCH /api/fahrzeuge/{id}/wartung - Markiert ein Fahrzeug als in Wartung.
     * 
     * @param id die ID des Fahrzeugs
     * @return Status 204 No Content
     */
    @PatchMapping("/{id}/wartung")
    public ResponseEntity<Void> markAsInMaintenance(@PathVariable Long id) {
        vehicleApplicationService.markVehicleAsInMaintenance(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/fahrzeuge - Gibt alle Fahrzeuge zurück.
     * 
     * @return Liste aller Fahrzeuge mit Status 200 OK
     */
    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        List<VehicleResponseDTO> vehicles = vehicleApplicationService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }
    
    /**
     * GET /api/fahrzeuge/{id} - Gibt ein einzelnes Fahrzeug zurück.
     * 
     * @param id die ID des Fahrzeugs
     * @return das Fahrzeug mit Status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        VehicleResponseDTO response = vehicleApplicationService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }
}

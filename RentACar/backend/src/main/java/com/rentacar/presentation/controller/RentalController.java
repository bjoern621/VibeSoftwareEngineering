package com.rentacar.presentation.controller;

import com.rentacar.application.service.RentalApplicationService;
import com.rentacar.presentation.dto.CheckOutRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Vermietungsprozesse (Check-out, Check-in).
 */
@RestController
@RequestMapping("/api/vermietung")
public class RentalController {

    private final RentalApplicationService rentalApplicationService;

    public RentalController(RentalApplicationService rentalApplicationService) {
        this.rentalApplicationService = rentalApplicationService;
    }

    /**
     * Führt den Check-out (Fahrzeugübergabe) durch.
     * 
     * @param buchungId ID der Buchung
     * @param request Check-out Daten (Kilometerstand, Zustand)
     * @return 200 OK bei Erfolg
     */
    @PostMapping("/{buchungId}/checkout")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<Void> performCheckOut(
            @PathVariable Long buchungId,
            @Valid @RequestBody CheckOutRequestDTO request) {
        
        rentalApplicationService.performCheckOut(
                buchungId,
                request.getMileage(),
                request.getFuelLevel(),
                request.getCleanliness(),
                request.getDamagesDescription()
        );
        
        return ResponseEntity.ok().build();
    }
}

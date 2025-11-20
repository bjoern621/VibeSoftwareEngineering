package com.rentacar.presentation.controller;

import com.rentacar.application.service.VehicleTypeApplicationService;
import com.rentacar.domain.model.VehicleType;
import com.rentacar.presentation.dto.VehicleTypeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller für Fahrzeugtypen-Verwaltung.
 * 
 * Bietet Endpoints zum Abrufen von Fahrzeugtyp-Informationen.
 */
@RestController
@RequestMapping("/api/vehicle-types")
public class VehicleTypeController {
    
    private final VehicleTypeApplicationService vehicleTypeApplicationService;
    
    public VehicleTypeController(VehicleTypeApplicationService vehicleTypeApplicationService) {
        this.vehicleTypeApplicationService = vehicleTypeApplicationService;
    }
    
    /**
     * GET /api/vehicle-types
     * 
     * Gibt alle verfügbaren Fahrzeugtypen zurück.
     * 
     * @return Liste aller Fahrzeugtypen
     */
    @GetMapping
    public ResponseEntity<List<VehicleTypeResponse>> getAllVehicleTypes() {
        List<VehicleTypeResponse> vehicleTypes = vehicleTypeApplicationService
            .getAllVehicleTypes()
            .stream()
            .map(VehicleTypeResponse::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(vehicleTypes);
    }
    
    /**
     * GET /api/vehicle-types/{typeName}
     * 
     * Gibt Details zu einem spezifischen Fahrzeugtyp zurück.
     * 
     * @param typeName Name des Fahrzeugtyps (z.B. "COMPACT_CAR")
     * @return Fahrzeugtyp-Details oder 404 Not Found
     */
    @GetMapping("/{typeName}")
    public ResponseEntity<VehicleTypeResponse> getVehicleTypeByName(
        @PathVariable String typeName
    ) {
        return vehicleTypeApplicationService
            .getVehicleTypeByName(typeName)
            .map(VehicleTypeResponse::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

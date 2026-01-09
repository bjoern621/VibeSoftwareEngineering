package com.rentacar.application.service;

import com.rentacar.domain.model.VehicleType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application Service für Fahrzeugtypen-Verwaltung.
 * 
 * Orchestriert Use Cases rund um Fahrzeugtypen.
 * Da VehicleType ein Enum ist, benötigen wir kein Repository.
 */
@Service
public class VehicleTypeApplicationService {
    
    /**
     * Gibt alle verfügbaren Fahrzeugtypen zurück.
     * 
     * @return Liste aller Fahrzeugtypen
     */
    public List<VehicleType> getAllVehicleTypes() {
        return VehicleType.getAllTypes();
    }
    
    /**
     * Sucht einen Fahrzeugtyp anhand seines Namens.
     * 
     * @param typeName Name des Fahrzeugtyps (z.B. "COMPACT_CAR")
     * @return Optional mit dem gefundenen Fahrzeugtyp
     */
    public Optional<VehicleType> getVehicleTypeByName(String typeName) {
        return VehicleType.fromString(typeName);
    }
}

package com.rentacar.domain.exception;

import com.rentacar.domain.model.Vehicle;
import java.util.List;

public class VehicleNotAvailableException extends RuntimeException {
    private final List<Vehicle> alternativeVehicles;

    public VehicleNotAvailableException(String message, List<Vehicle> alternativeVehicles) {
        super(message);
        this.alternativeVehicles = alternativeVehicles;
    }

    public List<Vehicle> getAlternativeVehicles() {
        return alternativeVehicles;
    }
}

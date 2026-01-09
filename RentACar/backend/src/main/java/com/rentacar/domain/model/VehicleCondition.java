package com.rentacar.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object für den Zustand eines Fahrzeugs (Tank, Sauberkeit, Schäden).
 */
@Embeddable
public class VehicleCondition {

    @Column(name = "fuel_level", nullable = false)
    private String fuelLevel;

    @Column(name = "cleanliness", nullable = false)
    private String cleanliness;

    @Column(name = "damages_description")
    private String damagesDescription;

    protected VehicleCondition() {
        // JPA
    }

    public VehicleCondition(String fuelLevel, String cleanliness, String damagesDescription) {
        this.fuelLevel = Objects.requireNonNull(fuelLevel, "Fuel level must not be null");
        this.cleanliness = Objects.requireNonNull(cleanliness, "Cleanliness must not be null");
        this.damagesDescription = damagesDescription;
    }

    public String getFuelLevel() {
        return fuelLevel;
    }

    public String getCleanliness() {
        return cleanliness;
    }

    public String getDamagesDescription() {
        return damagesDescription;
    }
}

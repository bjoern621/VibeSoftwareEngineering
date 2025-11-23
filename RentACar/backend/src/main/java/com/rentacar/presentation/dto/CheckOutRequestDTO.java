package com.rentacar.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO für den Check-out Request.
 */
public class CheckOutRequestDTO {

    @NotNull(message = "Kilometerstand darf nicht null sein")
    @Min(value = 0, message = "Kilometerstand muss positiv sein")
    private Integer mileage;

    @NotBlank(message = "Tankfüllung darf nicht leer sein")
    private String fuelLevel;

    @NotBlank(message = "Sauberkeit darf nicht leer sein")
    private String cleanliness;

    private String damagesDescription;

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public String getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(String fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public String getCleanliness() {
        return cleanliness;
    }

    public void setCleanliness(String cleanliness) {
        this.cleanliness = cleanliness;
    }

    public String getDamagesDescription() {
        return damagesDescription;
    }

    public void setDamagesDescription(String damagesDescription) {
        this.damagesDescription = damagesDescription;
    }
}

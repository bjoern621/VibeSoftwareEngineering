package com.rentacar.presentation.dto;

import java.math.BigDecimal;

/**
 * DTO für Suchergebnisse der Fahrzeugsuche.
 * Enthält Fahrzeugdetails und Preisinformationen.
 */
public class VehicleSearchResultDTO extends VehicleResponseDTO {

    private BigDecimal pricePerDay;
    private BigDecimal estimatedTotalPrice;

    public VehicleSearchResultDTO() {
        super();
    }

    public VehicleSearchResultDTO(VehicleResponseDTO vehicle, BigDecimal pricePerDay, BigDecimal estimatedTotalPrice) {
        super(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getBrand(), vehicle.getModel(),
              vehicle.getYear(), vehicle.getMileage(), vehicle.getVehicleType(),
              vehicle.getStatus(), vehicle.getBranchId(), vehicle.getBranchName());
        this.pricePerDay = pricePerDay;
        this.estimatedTotalPrice = estimatedTotalPrice;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getEstimatedTotalPrice() {
        return estimatedTotalPrice;
    }

    public void setEstimatedTotalPrice(BigDecimal estimatedTotalPrice) {
        this.estimatedTotalPrice = estimatedTotalPrice;
    }
}

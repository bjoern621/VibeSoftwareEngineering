package com.rentacar.presentation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CreateBookingRequestDTO {

    @NotNull
    private Long vehicleId;

    @NotNull
    private Long pickupBranchId;

    @NotNull
    private Long returnBranchId;

    @NotNull
    @FutureOrPresent(message = "Abholzeitpunkt darf nicht in der Vergangenheit liegen")
    private LocalDateTime pickupDateTime;

    @NotNull
    @FutureOrPresent(message = "Rückgabezeitpunkt darf nicht in der Vergangenheit liegen")
    private LocalDateTime returnDateTime;

    private List<String> additionalServices;

    // Getters and Setters

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getPickupBranchId() {
        return pickupBranchId;
    }

    public void setPickupBranchId(Long pickupBranchId) {
        this.pickupBranchId = pickupBranchId;
    }

    public Long getReturnBranchId() {
        return returnBranchId;
    }

    public void setReturnBranchId(Long returnBranchId) {
        this.returnBranchId = returnBranchId;
    }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public LocalDateTime getReturnDateTime() {
        return returnDateTime;
    }

    public void setReturnDateTime(LocalDateTime returnDateTime) {
        this.returnDateTime = returnDateTime;
    }

    public List<String> getAdditionalServices() {
        return additionalServices;
    }

    public void setAdditionalServices(List<String> additionalServices) {
        this.additionalServices = additionalServices;
    }
}

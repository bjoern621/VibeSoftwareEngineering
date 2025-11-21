package com.rentacar.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO für die Preisberechnung einer Miete.
 */
public class PriceCalculationRequestDTO {
    
    @NotNull(message = "Fahrzeugtyp darf nicht null sein")
    private String vehicleType;
    
    @NotNull(message = "Abholzeitpunkt darf nicht null sein")
    @Future(message = "Abholzeitpunkt muss in der Zukunft liegen")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pickupDateTime;
    
    @NotNull(message = "Rückgabezeitpunkt darf nicht null sein")
    @Future(message = "Rückgabezeitpunkt muss in der Zukunft liegen")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnDateTime;
    
    private List<String> additionalServices = new ArrayList<>();
    
    public PriceCalculationRequestDTO() {
    }
    
    public PriceCalculationRequestDTO(
        String vehicleType,
        LocalDateTime pickupDateTime,
        LocalDateTime returnDateTime,
        List<String> additionalServices
    ) {
        this.vehicleType = vehicleType;
        this.pickupDateTime = pickupDateTime;
        this.returnDateTime = returnDateTime;
        this.additionalServices = additionalServices != null ? additionalServices : new ArrayList<>();
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
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
        this.additionalServices = additionalServices != null ? additionalServices : new ArrayList<>();
    }
}

package com.rentacar.presentation.dto;

import com.rentacar.domain.model.VehicleType;

import java.math.BigDecimal;

/**
 * Response DTO für Fahrzeugtyp-Informationen.
 * 
 * Wird verwendet, um Fahrzeugtypen über die REST API zu exponieren.
 */
public class VehicleTypeResponse {
    
    private String name;
    private String displayName;
    private String category;
    private String priceClass;
    private BigDecimal dailyBaseRate;
    private int passengerCapacity;
    
    // Default-Konstruktor für Jackson
    public VehicleTypeResponse() {
    }
    
    // Konstruktor mit allen Feldern
    public VehicleTypeResponse(
        String name,
        String displayName,
        String category,
        String priceClass,
        BigDecimal dailyBaseRate,
        int passengerCapacity
    ) {
        this.name = name;
        this.displayName = displayName;
        this.category = category;
        this.priceClass = priceClass;
        this.dailyBaseRate = dailyBaseRate;
        this.passengerCapacity = passengerCapacity;
    }
    
    /**
     * Factory Method: Erstellt ein Response DTO aus einem VehicleType Enum.
     * 
     * @param vehicleType Fahrzeugtyp aus der Domain
     * @return Response DTO für die REST API
     */
    public static VehicleTypeResponse fromDomain(VehicleType vehicleType) {
        return new VehicleTypeResponse(
            vehicleType.name(),
            vehicleType.getDisplayName(),
            vehicleType.getCategory(),
            vehicleType.getPriceClass().name(),
            vehicleType.getDailyBaseRate(),
            vehicleType.getPassengerCapacity()
        );
    }
    
    // Getter und Setter
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getPriceClass() {
        return priceClass;
    }
    
    public void setPriceClass(String priceClass) {
        this.priceClass = priceClass;
    }
    
    public BigDecimal getDailyBaseRate() {
        return dailyBaseRate;
    }
    
    public void setDailyBaseRate(BigDecimal dailyBaseRate) {
        this.dailyBaseRate = dailyBaseRate;
    }
    
    public int getPassengerCapacity() {
        return passengerCapacity;
    }
    
    public void setPassengerCapacity(int passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }
}

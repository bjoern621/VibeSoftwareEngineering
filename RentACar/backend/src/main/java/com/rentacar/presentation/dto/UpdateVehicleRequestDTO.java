package com.rentacar.presentation.dto;

import com.rentacar.domain.model.VehicleType;
import jakarta.validation.constraints.*;

/**
 * Request DTO für die Aktualisierung eines bestehenden Fahrzeugs.
 * 
 * Enthält alle änderbaren Felder eines Fahrzeugs.
 * Das Kennzeichen kann nicht geändert werden (Geschäftsregel).
 */
public class UpdateVehicleRequestDTO {
    
    @NotBlank(message = "Marke darf nicht leer sein")
    @Size(min = 1, max = 100, message = "Marke muss zwischen 1 und 100 Zeichen lang sein")
    private String brand;
    
    @NotBlank(message = "Modell darf nicht leer sein")
    @Size(min = 1, max = 100, message = "Modell muss zwischen 1 und 100 Zeichen lang sein")
    private String model;
    
    @NotNull(message = "Baujahr darf nicht null sein")
    @Min(value = 1900, message = "Baujahr muss mindestens 1900 sein")
    @Max(value = 2026, message = "Baujahr darf maximal 2026 sein")
    private Integer year;
    
    @NotNull(message = "Kilometerstand darf nicht null sein")
    @Min(value = 0, message = "Kilometerstand darf nicht negativ sein")
    @Max(value = 10000000, message = "Kilometerstand ist unrealistisch hoch")
    private Integer mileage;
    
    @NotNull(message = "Fahrzeugtyp darf nicht null sein")
    private VehicleType vehicleType;
    
    @NotNull(message = "Filial-ID darf nicht null sein")
    @Positive(message = "Filial-ID muss positiv sein")
    private Long branchId;
    
    /**
     * Standardkonstruktor für Jackson Deserialisierung.
     */
    public UpdateVehicleRequestDTO() {
    }
    
    /**
     * Vollständiger Konstruktor für Tests und manuelle Erstellung.
     */
    public UpdateVehicleRequestDTO(String brand, String model, Integer year, 
                                Integer mileage, VehicleType vehicleType, 
                                Long branchId) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.vehicleType = vehicleType;
        this.branchId = branchId;
    }
    
    // Getter und Setter
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMileage() {
        return mileage;
    }
    
    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public Long getBranchId() {
        return branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
}

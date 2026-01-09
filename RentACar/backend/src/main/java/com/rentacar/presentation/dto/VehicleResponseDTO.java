package com.rentacar.presentation.dto;

import com.rentacar.domain.model.Vehicle;
import com.rentacar.domain.model.VehicleStatus;
import com.rentacar.domain.model.VehicleType;

/**
 * Response DTO für Fahrzeugdaten.
 * 
 * Repräsentiert ein Fahrzeug in der REST API Antwort.
 * Schützt die Domain Entity vor direkter Exposition.
 */
public class VehicleResponseDTO {
    
    private Long id;
    private String licensePlate;
    private String brand;
    private String model;
    private Integer year;
    private Integer mileage;
    private VehicleType vehicleType;
    private VehicleStatus status;
    private Long branchId;
    private String branchName;
    
    /**
     * Standardkonstruktor für Jackson Serialisierung.
     */
    public VehicleResponseDTO() {
    }
    
    /**
     * Vollständiger Konstruktor.
     */
    public VehicleResponseDTO(Long id, String licensePlate, String brand, String model,
                          Integer year, Integer mileage, VehicleType vehicleType,
                          VehicleStatus status, Long branchId, String branchName) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.vehicleType = vehicleType;
        this.status = status;
        this.branchId = branchId;
        this.branchName = branchName;
    }
    
    /**
     * Factory-Methode zur Erstellung aus einer Vehicle Entity.
     * 
     * @param vehicle die Vehicle Entity
     * @return VehicleResponseDTO DTO
     */
    public static VehicleResponseDTO fromEntity(Vehicle vehicle) {
        return new VehicleResponseDTO(
            vehicle.getId(),
            vehicle.getLicensePlate().getValue(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getYear(),
            vehicle.getMileage().getKilometers(),
            vehicle.getVehicleType(),
            vehicle.getStatus(),
            vehicle.getBranch().getId(),
            vehicle.getBranch().getName()
        );
    }
    
    // Getter und Setter
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
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
    
    public VehicleStatus getStatus() {
        return status;
    }
    
    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
    
    public Long getBranchId() {
        return branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}

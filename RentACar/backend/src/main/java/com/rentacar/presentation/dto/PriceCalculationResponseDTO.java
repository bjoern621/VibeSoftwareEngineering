package com.rentacar.presentation.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO für die Preisberechnung einer Miete.
 */
public class PriceCalculationResponseDTO {
    
    private String vehicleType;
    private String vehicleTypeDisplayName;
    private int numberOfDays;
    private BigDecimal dailyRate;
    private BigDecimal basePrice;
    private List<AdditionalServiceItemDTO> additionalServices;
    private BigDecimal additionalServicesPrice;
    private BigDecimal totalPrice;
    
    public PriceCalculationResponseDTO() {
        this.additionalServices = new ArrayList<>();
    }
    
    public PriceCalculationResponseDTO(
        String vehicleType,
        String vehicleTypeDisplayName,
        int numberOfDays,
        BigDecimal dailyRate,
        BigDecimal basePrice,
        List<AdditionalServiceItemDTO> additionalServices,
        BigDecimal additionalServicesPrice,
        BigDecimal totalPrice
    ) {
        this.vehicleType = vehicleType;
        this.vehicleTypeDisplayName = vehicleTypeDisplayName;
        this.numberOfDays = numberOfDays;
        this.dailyRate = dailyRate;
        this.basePrice = basePrice;
        this.additionalServices = additionalServices != null ? additionalServices : new ArrayList<>();
        this.additionalServicesPrice = additionalServicesPrice;
        this.totalPrice = totalPrice;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public String getVehicleTypeDisplayName() {
        return vehicleTypeDisplayName;
    }
    
    public void setVehicleTypeDisplayName(String vehicleTypeDisplayName) {
        this.vehicleTypeDisplayName = vehicleTypeDisplayName;
    }
    
    public int getNumberOfDays() {
        return numberOfDays;
    }
    
    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
    
    public BigDecimal getDailyRate() {
        return dailyRate;
    }
    
    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public List<AdditionalServiceItemDTO> getAdditionalServices() {
        return additionalServices;
    }
    
    public void setAdditionalServices(List<AdditionalServiceItemDTO> additionalServices) {
        this.additionalServices = additionalServices;
    }
    
    public BigDecimal getAdditionalServicesPrice() {
        return additionalServicesPrice;
    }
    
    public void setAdditionalServicesPrice(BigDecimal additionalServicesPrice) {
        this.additionalServicesPrice = additionalServicesPrice;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    /**
     * DTO für eine einzelne Zusatzleistung in der Preisberechnung.
     */
    public static class AdditionalServiceItemDTO {
        private String serviceType;
        private String displayName;
        private BigDecimal dailyPrice;
        private BigDecimal totalPrice;
        
        public AdditionalServiceItemDTO() {
        }
        
        public AdditionalServiceItemDTO(
            String serviceType,
            String displayName,
            BigDecimal dailyPrice,
            BigDecimal totalPrice
        ) {
            this.serviceType = serviceType;
            this.displayName = displayName;
            this.dailyPrice = dailyPrice;
            this.totalPrice = totalPrice;
        }
        
        public String getServiceType() {
            return serviceType;
        }
        
        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public BigDecimal getDailyPrice() {
            return dailyPrice;
        }
        
        public void setDailyPrice(BigDecimal dailyPrice) {
            this.dailyPrice = dailyPrice;
        }
        
        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}

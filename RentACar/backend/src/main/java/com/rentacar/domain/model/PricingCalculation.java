package com.rentacar.domain.model;

import com.rentacar.domain.exception.InvalidPricingDataException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value Object für das Ergebnis einer Preisberechnung.
 * 
 * Kapselt alle Details einer Mietpreisberechnung inklusive Grundpreis,
 * Zusatzleistungen und Gesamtpreis.
 */
public class PricingCalculation {
    
    private final VehicleType vehicleType;
    private final int numberOfDays;
    private final BigDecimal basePrice;
    private final List<AdditionalServiceItem> additionalServices;
    private final BigDecimal additionalServicesPrice;
    private final BigDecimal totalPrice;
    
    /**
     * Erstellt eine neue Preisberechnung.
     * 
     * @param vehicleType Fahrzeugtyp
     * @param numberOfDays Anzahl der Miettage
     * @param basePrice Grundpreis (Tagespreis × Tage)
     * @param additionalServices Liste der gebuchten Zusatzleistungen
     * @param additionalServicesPrice Gesamtpreis aller Zusatzleistungen
     * @param totalPrice Gesamtpreis (Grundpreis + Zusatzleistungen)
     */
    public PricingCalculation(
        VehicleType vehicleType,
        int numberOfDays,
        BigDecimal basePrice,
        List<AdditionalServiceItem> additionalServices,
        BigDecimal additionalServicesPrice,
        BigDecimal totalPrice
    ) {
        if (vehicleType == null) {
            throw new InvalidPricingDataException("Fahrzeugtyp darf nicht null sein");
        }
        if (basePrice == null) {
            throw new InvalidPricingDataException("Grundpreis darf nicht null sein");
        }
        if (additionalServicesPrice == null) {
            throw new InvalidPricingDataException("Preis für Zusatzleistungen darf nicht null sein");
        }
        if (totalPrice == null) {
            throw new InvalidPricingDataException("Gesamtpreis darf nicht null sein");
        }

        this.vehicleType = vehicleType;
        this.numberOfDays = numberOfDays;
        this.basePrice = basePrice;
        this.additionalServices = new ArrayList<>(additionalServices);
        this.additionalServicesPrice = additionalServicesPrice;
        this.totalPrice = totalPrice;
    }
    
    /**
     * @return Fahrzeugtyp
     */
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    /**
     * @return Anzahl der Miettage
     */
    public int getNumberOfDays() {
        return numberOfDays;
    }
    
    /**
     * @return Grundpreis (Tagespreis × Anzahl Tage)
     */
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    /**
     * @return Unveränderliche Liste der Zusatzleistungen
     */
    public List<AdditionalServiceItem> getAdditionalServices() {
        return Collections.unmodifiableList(additionalServices);
    }
    
    /**
     * @return Gesamtpreis aller Zusatzleistungen
     */
    public BigDecimal getAdditionalServicesPrice() {
        return additionalServicesPrice;
    }
    
    /**
     * @return Gesamtpreis (Grundpreis + Zusatzleistungen)
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PricingCalculation that = (PricingCalculation) o;
        return numberOfDays == that.numberOfDays &&
               vehicleType == that.vehicleType &&
               Objects.equals(basePrice, that.basePrice) &&
               Objects.equals(additionalServices, that.additionalServices) &&
               Objects.equals(additionalServicesPrice, that.additionalServicesPrice) &&
               Objects.equals(totalPrice, that.totalPrice);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
            vehicleType, 
            numberOfDays, 
            basePrice, 
            additionalServices, 
            additionalServicesPrice, 
            totalPrice
        );
    }
    
    @Override
    public String toString() {
        return "PricingCalculation{" +
               "vehicleType=" + vehicleType +
               ", numberOfDays=" + numberOfDays +
               ", basePrice=" + basePrice +
               ", additionalServices=" + additionalServices.size() +
               ", additionalServicesPrice=" + additionalServicesPrice +
               ", totalPrice=" + totalPrice +
               '}';
    }
    
    /**
     * Repräsentiert eine einzelne Zusatzleistung in der Preisberechnung.
     */
    public static class AdditionalServiceItem {
        private final AdditionalServiceType serviceType;
        private final BigDecimal price;
        
        public AdditionalServiceItem(AdditionalServiceType serviceType, BigDecimal price) {
            if (serviceType == null) {
                throw new InvalidPricingDataException("Service type must not be null");
            }
            if (price == null) {
                throw new InvalidPricingDataException("Price must not be null");
            }
            this.serviceType = serviceType;
            this.price = price;
        }
        
        public AdditionalServiceType getServiceType() {
            return serviceType;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AdditionalServiceItem that = (AdditionalServiceItem) o;
            return serviceType == that.serviceType &&
                   Objects.equals(price, that.price);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(serviceType, price);
        }
        
        @Override
        public String toString() {
            return "AdditionalServiceItem{" +
                   "serviceType=" + serviceType +
                   ", price=" + price +
                   '}';
        }
    }
}

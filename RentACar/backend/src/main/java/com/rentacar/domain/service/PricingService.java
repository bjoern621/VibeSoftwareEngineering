package com.rentacar.domain.service;

import com.rentacar.domain.model.AdditionalServiceType;
import com.rentacar.domain.model.DateRange;
import com.rentacar.domain.model.PricingCalculation;
import com.rentacar.domain.model.PricingCalculation.AdditionalServiceItem;
import com.rentacar.domain.model.VehicleType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain Service für die Berechnung von Mietpreisen.
 * 
 * Dieser Service enthält die zentrale Business-Logik zur Preisberechnung
 * basierend auf Fahrzeugtyp, Mietdauer und Zusatzleistungen.
 * 
 * Reine Domain-Logik ohne Framework-Abhängigkeiten (keine Spring-Annotations).
 */
public class PricingService {
    
    /**
     * Berechnet den Gesamtpreis für eine Miete.
     * 
     * Berechnungslogik:
     * - Grundpreis = Tagespreis des Fahrzeugtyps × Anzahl der Miettage
     * - Zusatzleistungen = Summe aller (Preis pro Zusatzleistung × Anzahl Tage)
     * - Gesamtpreis = Grundpreis + Zusatzleistungen
     * 
     * @param vehicleType Fahrzeugtyp mit Tagespreis
     * @param rentalPeriod Mietzeitraum
     * @param additionalServices Liste der gewünschten Zusatzleistungen (kann leer sein)
     * @return Vollständige Preisberechnung mit allen Details
     * @throws IllegalArgumentException wenn Parameter ungültig sind
     */
    public PricingCalculation calculatePrice(
        VehicleType vehicleType,
        DateRange rentalPeriod,
        List<AdditionalServiceType> additionalServices
    ) {
        Objects.requireNonNull(vehicleType, "Fahrzeugtyp darf nicht null sein");
        Objects.requireNonNull(rentalPeriod, "Mietzeitraum darf nicht null sein");
        Objects.requireNonNull(additionalServices, "Liste der Zusatzleistungen darf nicht null sein");
        
        int numberOfDays = rentalPeriod.getDays();
        
        // Grundpreis berechnen: Tagespreis × Anzahl Tage
        BigDecimal dailyRate = vehicleType.getDailyBaseRate();
        BigDecimal basePrice = dailyRate.multiply(BigDecimal.valueOf(numberOfDays));
        
        // Zusatzleistungen berechnen
        List<AdditionalServiceItem> serviceItems = new ArrayList<>();
        BigDecimal additionalServicesTotal = BigDecimal.ZERO;
        
        for (AdditionalServiceType service : additionalServices) {
            BigDecimal servicePrice = service.calculatePrice(numberOfDays);
            serviceItems.add(new AdditionalServiceItem(service, servicePrice));
            additionalServicesTotal = additionalServicesTotal.add(servicePrice);
        }
        
        // Gesamtpreis berechnen
        BigDecimal totalPrice = basePrice.add(additionalServicesTotal);
        
        return new PricingCalculation(
            vehicleType,
            numberOfDays,
            basePrice,
            serviceItems,
            additionalServicesTotal,
            totalPrice
        );
    }
    
    /**
     * Berechnet nur den Grundpreis ohne Zusatzleistungen.
     * 
     * @param vehicleType Fahrzeugtyp
     * @param rentalPeriod Mietzeitraum
     * @return Grundpreis (Tagespreis × Anzahl Tage)
     */
    public BigDecimal calculateBasePrice(VehicleType vehicleType, DateRange rentalPeriod) {
        Objects.requireNonNull(vehicleType, "Fahrzeugtyp darf nicht null sein");
        Objects.requireNonNull(rentalPeriod, "Mietzeitraum darf nicht null sein");
        
        int numberOfDays = rentalPeriod.getDays();
        return vehicleType.getDailyBaseRate().multiply(BigDecimal.valueOf(numberOfDays));
    }
    
    /**
     * Berechnet den Gesamtpreis aller Zusatzleistungen über den Mietzeitraum.
     * 
     * @param additionalServices Liste der Zusatzleistungen
     * @param numberOfDays Anzahl der Miettage
     * @return Gesamtpreis aller Zusatzleistungen
     */
    public BigDecimal calculateAdditionalServicesPrice(
        List<AdditionalServiceType> additionalServices,
        int numberOfDays
    ) {
        Objects.requireNonNull(additionalServices, "Liste der Zusatzleistungen darf nicht null sein");
        
        if (numberOfDays < 1) {
            throw new IllegalArgumentException("Anzahl der Tage muss mindestens 1 sein");
        }
        
        return additionalServices.stream()
            .map(service -> service.calculatePrice(numberOfDays))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

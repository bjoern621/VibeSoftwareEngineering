package com.travelreimburse.domain.model;

/**
 * Enum für verschiedene Visum-Typen
 */
public enum VisaType {
    /**
     * Geschäftsvisum für Dienstreisen
     */
    BUSINESS,
    
    /**
     * Touristenvisum
     */
    TOURIST,
    
    /**
     * Arbeitsvisum für längere Aufenthalte
     */
    WORK,
    
    /**
     * Transitvisum für Zwischenstopps
     */
    TRANSIT,
    
    /**
     * E-Visum (elektronisches Visum)
     */
    E_VISA,
    
    /**
     * Visa on Arrival (bei Ankunft erhältlich)
     */
    VISA_ON_ARRIVAL
}

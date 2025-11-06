package com.travelreimburse.domain.model;

/**
 * Enum für verschiedene Verkehrsmittel
 * Teil des Domain Models für Reiserouten
 */
public enum TransportationType {
    /**
     * Flugreise
     */
    FLIGHT,
    
    /**
     * Bahnreise (Zug, S-Bahn, etc.)
     */
    TRAIN,
    
    /**
     * Mietwagen
     */
    CAR_RENTAL,
    
    /**
     * Taxi oder Ridesharing (Uber, etc.)
     */
    TAXI,
    
    /**
     * Öffentliche Verkehrsmittel (Bus, U-Bahn, etc.)
     */
    PUBLIC_TRANSPORT,
    
    /**
     * Eigener PKW (Kilometerpauschale)
     */
    OWN_CAR,
    
    /**
     * Sonstige Verkehrsmittel
     */
    OTHER
}

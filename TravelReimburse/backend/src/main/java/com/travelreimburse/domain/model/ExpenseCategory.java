package com.travelreimburse.domain.model;

/**
 * Enum für Ausgabenkategorien in Reisekostenabrechnungen
 * 
 * Definiert die verschiedenen Kostenarten für die Reiserichtlinien gelten
 */
public enum ExpenseCategory {
    
    /**
     * Unterkunft (Hotel, Airbnb, etc.)
     */
    ACCOMMODATION,
    
    /**
     * Verpflegung (Frühstück, Mittagessen, Abendessen)
     */
    MEALS,
    
    /**
     * Transport (Flug, Bahn, Taxi, Mietwagen)
     */
    TRANSPORTATION,
    
    /**
     * Treibstoff für Dienstwagen oder Mietwagen
     */
    FUEL,
    
    /**
     * Parken und Maut
     */
    PARKING_TOLLS,
    
    /**
     * Sonstige Ausgaben (Visum, Impfungen, etc.)
     */
    OTHER
}

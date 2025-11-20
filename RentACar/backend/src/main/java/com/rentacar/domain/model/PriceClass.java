package com.rentacar.domain.model;

/**
 * Preisklassen für Fahrzeuge im RentACar-System.
 * 
 * Die Preisklasse bestimmt die Kategorie des Fahrzeugs im Hinblick auf
 * Komfort, Ausstattung und Mietpreis.
 */
public enum PriceClass {
    
    /**
     * Economy-Klasse: Einfache, kostengünstige Fahrzeuge
     */
    ECONOMY("Economy", "Basisausstattung, günstigster Preis"),
    
    /**
     * Standard-Klasse: Durchschnittlich ausgestattete Fahrzeuge
     */
    STANDARD("Standard", "Standardausstattung, gutes Preis-Leistungs-Verhältnis"),
    
    /**
     * Premium-Klasse: Gehobene Fahrzeuge mit erhöhtem Komfort
     */
    PREMIUM("Premium", "Gehobene Ausstattung, höherer Komfort"),
    
    /**
     * Luxury-Klasse: Luxusfahrzeuge mit Top-Ausstattung
     */
    LUXURY("Luxury", "Luxusausstattung, höchster Komfort");
    
    private final String displayName;
    private final String description;
    
    PriceClass(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * @return Anzeigename der Preisklasse
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return Beschreibung der Preisklasse
     */
    public String getDescription() {
        return description;
    }
}

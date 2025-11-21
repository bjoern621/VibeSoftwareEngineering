package com.rentacar.domain.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Zusatzleistungen, die bei einer Buchung hinzugefügt werden können.
 * 
 * Jede Zusatzleistung hat einen festen Preis pro Tag.
 */
public enum AdditionalServiceType {
    
    /**
     * Kindersitz - für Kinder bis 12 Jahre
     */
    CHILD_SEAT(
        "Kindersitz",
        "Sicherheitssitz für Kinder (bis 12 Jahre)",
        new BigDecimal("5.00")
    ),
    
    /**
     * Navigationssystem (GPS)
     */
    GPS(
        "Navigationssystem",
        "GPS-Navigationssystem mit aktuellen Karten",
        new BigDecimal("8.00")
    ),
    
    /**
     * Zusätzlicher Fahrer
     */
    ADDITIONAL_DRIVER(
        "Zusätzlicher Fahrer",
        "Berechtigung für einen weiteren Fahrer",
        new BigDecimal("12.00")
    ),
    
    /**
     * Vollkaskoversicherung ohne Selbstbeteiligung
     */
    FULL_INSURANCE(
        "Vollkasko ohne SB",
        "Vollkaskoversicherung ohne Selbstbeteiligung",
        new BigDecimal("15.00")
    ),
    
    /**
     * Winterreifen
     */
    WINTER_TIRES(
        "Winterreifen",
        "Winterreifen für sichere Fahrt bei Schnee und Eis",
        new BigDecimal("6.00")
    ),
    
    /**
     * Dachgepäckträger
     */
    ROOF_RACK(
        "Dachgepäckträger",
        "Gepäckträger für zusätzlichen Stauraum",
        new BigDecimal("7.00")
    );
    
    private final String displayName;
    private final String description;
    private final BigDecimal dailyPrice;
    
    AdditionalServiceType(String displayName, String description, BigDecimal dailyPrice) {
        this.displayName = displayName;
        this.description = description;
        this.dailyPrice = dailyPrice;
    }
    
    /**
     * @return Anzeigename der Zusatzleistung
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return Beschreibung der Zusatzleistung
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return Preis pro Tag in Euro
     */
    public BigDecimal getDailyPrice() {
        return dailyPrice;
    }
    
    /**
     * Berechnet den Gesamtpreis für diese Zusatzleistung über einen bestimmten Zeitraum.
     * 
     * @param days Anzahl der Tage
     * @return Gesamtpreis für die Zusatzleistung
     */
    public BigDecimal calculatePrice(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Anzahl der Tage muss mindestens 1 sein");
        }
        return dailyPrice.multiply(BigDecimal.valueOf(days));
    }
    
    /**
     * Findet eine Zusatzleistung anhand ihres Namens (case-insensitive).
     * 
     * @param name Name der Zusatzleistung (z.B. "CHILD_SEAT" oder "child_seat")
     * @return Optional mit der gefundenen Zusatzleistung oder empty
     */
    public static Optional<AdditionalServiceType> fromString(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(AdditionalServiceType.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * @return Liste aller verfügbaren Zusatzleistungen
     */
    public static List<AdditionalServiceType> getAllServices() {
        return Arrays.asList(AdditionalServiceType.values());
    }
}

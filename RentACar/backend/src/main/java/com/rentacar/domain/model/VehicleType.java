package com.rentacar.domain.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Fahrzeugtypen im RentACar-System.
 * 
 * Jeder Fahrzeugtyp hat spezifische Eigenschaften wie Kategorie, Preisklasse,
 * Grundtarif und Passagierkapazität.
 */
public enum VehicleType {
    
    /**
     * Kleinwagen - kompakte, kraftstoffsparende Fahrzeuge für den Stadtverkehr
     */
    COMPACT_CAR(
        "Kleinwagen",
        "Kompaktklasse",
        PriceClass.ECONOMY,
        new BigDecimal("29.99"),
        5
    ),
    
    /**
     * Limousine - komfortable Mittelklassefahrzeuge
     */
    SEDAN(
        "Limousine",
        "Mittelklasse",
        PriceClass.STANDARD,
        new BigDecimal("49.99"),
        5
    ),
    
    /**
     * SUV - geräumige Geländewagen mit erhöhter Sitzposition
     */
    SUV(
        "SUV",
        "Geländewagen",
        PriceClass.PREMIUM,
        new BigDecimal("79.99"),
        7
    ),
    
    /**
     * Transporter - Nutzfahrzeuge für Transporte und große Gruppen
     */
    VAN(
        "Transporter",
        "Nutzfahrzeug",
        PriceClass.STANDARD,
        new BigDecimal("69.99"),
        9
    );
    
    private final String displayName;
    private final String category;
    private final PriceClass priceClass;
    private final BigDecimal dailyBaseRate;
    private final int passengerCapacity;
    
    VehicleType(
        String displayName,
        String category,
        PriceClass priceClass,
        BigDecimal dailyBaseRate,
        int passengerCapacity
    ) {
        this.displayName = displayName;
        this.category = category;
        this.priceClass = priceClass;
        this.dailyBaseRate = dailyBaseRate;
        this.passengerCapacity = passengerCapacity;
    }
    
    /**
     * @return Anzeigename des Fahrzeugtyps (z.B. "Kleinwagen")
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @return Kategorie des Fahrzeugtyps (z.B. "Kompaktklasse")
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * @return Preisklasse des Fahrzeugtyps
     */
    public PriceClass getPriceClass() {
        return priceClass;
    }
    
    /**
     * @return Grundtarif pro Tag in Euro
     */
    public BigDecimal getDailyBaseRate() {
        return dailyBaseRate;
    }
    
    /**
     * @return Maximale Anzahl an Passagieren
     */
    public int getPassengerCapacity() {
        return passengerCapacity;
    }
    
    /**
     * Findet einen Fahrzeugtyp anhand seines Namens (case-insensitive).
     * 
     * @param name Name des Fahrzeugtyps (z.B. "COMPACT_CAR" oder "compact_car")
     * @return Optional mit dem gefundenen Fahrzeugtyp oder empty
     */
    public static Optional<VehicleType> fromString(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(VehicleType.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * @return Liste aller verfügbaren Fahrzeugtypen
     */
    public static List<VehicleType> getAllTypes() {
        return Arrays.asList(VehicleType.values());
    }
}

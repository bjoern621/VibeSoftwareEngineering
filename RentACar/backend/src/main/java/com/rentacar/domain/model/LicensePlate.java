package com.rentacar.domain.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object für ein Fahrzeugkennzeichen.
 * 
 * Repräsentiert ein eindeutiges Kennzeichen mit Validierung.
 * Immutabel gemäß DDD-Prinzipien.
 */
@Embeddable
public class LicensePlate {
    
    // Deutsches Kennzeichen-Format: 1-3 Buchstaben, 1-4 Ziffern, 1-2 Buchstaben
    // Beispiel: B-AB 1234, HH-XY 99
    private static final Pattern LICENSE_PLATE_PATTERN = 
        Pattern.compile("^[A-ZÄÖÜ]{1,3}-[A-Z]{1,2}\\s?\\d{1,4}[HE]?$");
    
    private String value;
    
    /**
     * Standardkonstruktor für JPA.
     */
    protected LicensePlate() {
    }
    
    /**
     * Erstellt ein neues Kennzeichen mit Validierung.
     * 
     * @param value das Kennzeichen als String
     * @throws IllegalArgumentException wenn das Kennzeichen ungültig ist
     */
    private LicensePlate(String value) {
        validate(value);
        this.value = value.toUpperCase().trim();
    }
    
    /**
     * Factory-Methode zum Erstellen eines Kennzeichens.
     * 
     * @param value das Kennzeichen als String
     * @return neues LicensePlate Value Object
     * @throws IllegalArgumentException wenn das Kennzeichen ungültig ist
     */
    public static LicensePlate of(String value) {
        return new LicensePlate(value);
    }
    
    /**
     * Validiert das Kennzeichen-Format.
     * 
     * @param value das zu validierende Kennzeichen
     * @throws IllegalArgumentException wenn das Kennzeichen ungültig ist
     */
    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Kennzeichen darf nicht null oder leer sein");
        }
        
        String normalized = value.toUpperCase().trim();
        if (!LICENSE_PLATE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Ungültiges Kennzeichen-Format: " + value + 
                ". Erwartetes Format: XX-YY 1234"
            );
        }
    }
    
    /**
     * @return das Kennzeichen als String
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicensePlate that = (LicensePlate) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

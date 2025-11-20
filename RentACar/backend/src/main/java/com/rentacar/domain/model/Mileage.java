package com.rentacar.domain.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object für den Kilometerstand eines Fahrzeugs.
 * 
 * Repräsentiert einen nicht-negativen Kilometerstand.
 * Immutabel gemäß DDD-Prinzipien.
 */
@Embeddable
public class Mileage {
    
    private Integer kilometers;
    
    /**
     * Standardkonstruktor für JPA.
     */
    protected Mileage() {
    }
    
    /**
     * Erstellt einen neuen Kilometerstand mit Validierung.
     * 
     * @param kilometers der Kilometerstand
     * @throws IllegalArgumentException wenn der Kilometerstand negativ ist
     */
    private Mileage(Integer kilometers) {
        validate(kilometers);
        this.kilometers = kilometers;
    }
    
    /**
     * Factory-Methode zum Erstellen eines Kilometerstands.
     * 
     * @param kilometers der Kilometerstand
     * @return neues Mileage Value Object
     * @throws IllegalArgumentException wenn der Kilometerstand negativ ist
     */
    public static Mileage of(Integer kilometers) {
        return new Mileage(kilometers);
    }
    
    /**
     * Erstellt einen Kilometerstand mit 0 km (Neufahrzeug).
     * 
     * @return neues Mileage Value Object mit 0 km
     */
    public static Mileage zero() {
        return new Mileage(0);
    }
    
    /**
     * Validiert den Kilometerstand.
     * 
     * @param kilometers der zu validierende Kilometerstand
     * @throws IllegalArgumentException wenn der Kilometerstand ungültig ist
     */
    private void validate(Integer kilometers) {
        if (kilometers == null) {
            throw new IllegalArgumentException("Kilometerstand darf nicht null sein");
        }
        if (kilometers < 0) {
            throw new IllegalArgumentException(
                "Kilometerstand darf nicht negativ sein: " + kilometers
            );
        }
    }
    
    /**
     * Erhöht den Kilometerstand um die angegebenen Kilometer.
     * 
     * @param additionalKilometers die hinzuzufügenden Kilometer
     * @return neues Mileage Value Object mit erhöhtem Kilometerstand
     * @throws IllegalArgumentException wenn die zusätzlichen Kilometer negativ sind
     */
    public Mileage add(Integer additionalKilometers) {
        if (additionalKilometers < 0) {
            throw new IllegalArgumentException(
                "Zusätzliche Kilometer dürfen nicht negativ sein: " + additionalKilometers
            );
        }
        return new Mileage(this.kilometers + additionalKilometers);
    }
    
    /**
     * Prüft, ob dieser Kilometerstand größer ist als ein anderer.
     * 
     * @param other der zu vergleichende Kilometerstand
     * @return true wenn dieser Kilometerstand größer ist
     */
    public boolean isGreaterThan(Mileage other) {
        return this.kilometers > other.kilometers;
    }
    
    /**
     * Prüft, ob dieser Kilometerstand kleiner ist als ein anderer.
     * 
     * @param other der zu vergleichende Kilometerstand
     * @return true wenn dieser Kilometerstand kleiner ist
     */
    public boolean isLessThan(Mileage other) {
        return this.kilometers < other.kilometers;
    }
    
    /**
     * @return der Kilometerstand als Integer
     */
    public Integer getKilometers() {
        return kilometers;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mileage mileage = (Mileage) o;
        return Objects.equals(kilometers, mileage.kilometers);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(kilometers);
    }
    
    @Override
    public String toString() {
        return kilometers + " km";
    }
}

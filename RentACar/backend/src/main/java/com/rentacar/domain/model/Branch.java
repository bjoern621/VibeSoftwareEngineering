package com.rentacar.domain.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Branch (Filiale) Aggregate Root.
 * 
 * Repräsentiert eine Vermietungsfiliale mit Standort und Kontaktdaten.
 * Business-Logik wird durch expressive Methoden anstelle von Settern implementiert.
 */
@Entity
@Table(name = "branches")
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String address;
    
    @Column(name = "opening_hours")
    private String openingHours;
    
    /**
     * Standardkonstruktor für JPA.
     */
    protected Branch() {
    }
    
    /**
     * Erstellt eine neue Filiale mit Validierung.
     * 
     * @param name der Name der Filiale
     * @param address die Adresse der Filiale
     * @param openingHours die Öffnungszeiten
     * @throws IllegalArgumentException wenn Pflichtfelder ungültig sind
     */
    public Branch(String name, String address, String openingHours) {
        validateName(name);
        validateAddress(address);
        
        this.name = name;
        this.address = address;
        this.openingHours = openingHours;
    }
    
    /**
     * Aktualisiert die Adresse der Filiale.
     * 
     * @param newAddress die neue Adresse
     * @throws IllegalArgumentException wenn die Adresse ungültig ist
     */
    public void updateAddress(String newAddress) {
        validateAddress(newAddress);
        this.address = newAddress;
    }
    
    /**
     * Aktualisiert die Öffnungszeiten der Filiale.
     * 
     * @param newOpeningHours die neuen Öffnungszeiten
     */
    public void updateOpeningHours(String newOpeningHours) {
        this.openingHours = newOpeningHours;
    }
    
    /**
     * Validiert den Filialnamen.
     * 
     * @param name der zu validierende Name
     * @throws IllegalArgumentException wenn der Name ungültig ist
     */
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Filialname darf nicht null oder leer sein");
        }
    }
    
    /**
     * Validiert die Adresse.
     * 
     * @param address die zu validierende Adresse
     * @throws IllegalArgumentException wenn die Adresse ungültig ist
     */
    private void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Filialadresse darf nicht null oder leer sein");
        }
    }
    
    // Getter
    
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(id, branch.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

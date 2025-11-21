package com.rentacar.domain.model;

import com.rentacar.infrastructure.persistence.converter.EncryptedStringConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object für Adressdaten.
 * Immutable und DSGVO-konform durch Verschlüsselung auf Persistenzebene.
 */
@Embeddable
public class Address {

    @Convert(converter = EncryptedStringConverter.class)
    private final String street;
    
    @Convert(converter = EncryptedStringConverter.class)
    private final String postalCode;
    
    @Convert(converter = EncryptedStringConverter.class)
    private final String city;

    // JPA benötigt einen Default-Konstruktor
    protected Address() {
        this.street = null;
        this.postalCode = null;
        this.city = null;
    }

    /**
     * Erstellt eine neue Adresse.
     *
     * @param street     Straße mit Hausnummer
     * @param postalCode Postleitzahl (5-stellig)
     * @param city       Stadt
     * @throws IllegalArgumentException wenn Parameter ungültig sind
     */
    public Address(String street, String postalCode, String city) {
        validateStreet(street);
        validatePostalCode(postalCode);
        validateCity(city);

        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
    }

    private void validateStreet(String street) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Straße darf nicht leer sein");
        }
        if (street.length() < 3 || street.length() > 100) {
            throw new IllegalArgumentException("Straße muss zwischen 3 und 100 Zeichen lang sein");
        }
    }

    private void validatePostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postleitzahl darf nicht leer sein");
        }
        if (!postalCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("Postleitzahl muss genau 5 Ziffern enthalten");
        }
    }

    private void validateCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Stadt darf nicht leer sein");
        }
        if (city.length() < 2 || city.length() > 100) {
            throw new IllegalArgumentException("Stadt muss zwischen 2 und 100 Zeichen lang sein");
        }
    }

    public String getStreet() {
        return street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(city, address.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, postalCode, city);
    }

    @Override
    public String toString() {
        return street + ", " + postalCode + " " + city;
    }
}

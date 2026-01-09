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

    private static final int MIN_STREET_LENGTH = 3;
    private static final int MAX_STREET_LENGTH = 100;
    private static final int MIN_CITY_LENGTH = 2;
    private static final int MAX_CITY_LENGTH = 100;
    private static final String POSTAL_CODE_REGEX = "\\d{5}";

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
     * @throws com.rentacar.domain.exception.InvalidAddressException wenn Parameter ungültig sind
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
            throw new com.rentacar.domain.exception.InvalidAddressException(
                "street", "Straße darf nicht leer sein");
        }
        if (street.length() < MIN_STREET_LENGTH || street.length() > MAX_STREET_LENGTH) {
            throw new com.rentacar.domain.exception.InvalidAddressException(
                "street", "Straße muss zwischen " + MIN_STREET_LENGTH + " und " + MAX_STREET_LENGTH + " Zeichen lang sein");
        }
    }

    private void validatePostalCode(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            throw new com.rentacar.domain.exception.InvalidAddressException(
                "postalCode", "Postleitzahl darf nicht leer sein");
        }
        if (!postalCode.matches(POSTAL_CODE_REGEX)) {
            throw new com.rentacar.domain.exception.InvalidAddressException(
                "postalCode", "Postleitzahl muss genau 5 Ziffern enthalten");
        }
    }

    private void validateCity(String city) {
        if (city == null || city.isBlank()) {
            throw new com.rentacar.domain.exception.InvalidAddressException(
                "city", "Stadt darf nicht leer sein");
        }
        if (city.length() < MIN_CITY_LENGTH || city.length() > MAX_CITY_LENGTH) {
            throw new com.rentacar.domain.exception.InvalidAddressException(
                "city", "Stadt muss zwischen " + MIN_CITY_LENGTH + " und " + MAX_CITY_LENGTH + " Zeichen lang sein");
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

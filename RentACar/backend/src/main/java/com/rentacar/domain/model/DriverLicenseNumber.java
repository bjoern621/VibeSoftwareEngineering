package com.rentacar.domain.model;

import com.rentacar.infrastructure.persistence.converter.EncryptedStringConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object für Führerscheinnummer.
 * Immutable und mit Validierung für deutsches Führerscheinformat.
 */
@Embeddable
public class DriverLicenseNumber {

    private static final int LICENSE_NUMBER_LENGTH = 11;
    private static final String LICENSE_NUMBER_REGEX = "[A-Z0-9]{" + LICENSE_NUMBER_LENGTH + "}";

    @Convert(converter = EncryptedStringConverter.class)
    private final String number;

    // JPA benötigt einen Default-Konstruktor
    protected DriverLicenseNumber() {
        this.number = null;
    }

    /**
     * Erstellt eine neue Führerscheinnummer.
     *
     * @param number Führerscheinnummer (deutsches Format: 11 Zeichen alphanumerisch)
     * @throws com.rentacar.domain.exception.InvalidDriverLicenseException wenn die Nummer ungültig ist
     */
    public DriverLicenseNumber(String number) {
        validateNumber(number);
        this.number = number.toUpperCase();
    }

    private void validateNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new com.rentacar.domain.exception.InvalidDriverLicenseException(
                "Führerscheinnummer darf nicht leer sein");
        }

        String normalized = number.toUpperCase().replaceAll("\\s", "");

        // Deutsches Führerscheinformat: 11 Zeichen (Buchstaben und Ziffern)
        // Format: z.B. B123456789XY
        if (normalized.length() != LICENSE_NUMBER_LENGTH) {
            throw new com.rentacar.domain.exception.InvalidDriverLicenseException(
                "Führerscheinnummer muss genau " + LICENSE_NUMBER_LENGTH + " Zeichen lang sein (aktuell: " + normalized.length() + ")");
        }

        if (!normalized.matches(LICENSE_NUMBER_REGEX)) {
            throw new com.rentacar.domain.exception.InvalidDriverLicenseException(
                "Führerscheinnummer darf nur Buchstaben und Ziffern enthalten");
        }
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverLicenseNumber that = (DriverLicenseNumber) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return number;
    }
}

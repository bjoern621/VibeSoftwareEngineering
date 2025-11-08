package com.travelreimburse.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object für ISO 3166-1 Alpha-2 Ländercodes
 * Unveränderlich - repräsentiert einen standardisierten Ländercode
 * 
 * DDD: Immutable Value Object ohne Identität
 */
@Embeddable
public class CountryCode {

    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");

    @Column(name = "country_code", nullable = false, length = 2)
    private final String code;

    // JPA benötigt Default-Konstruktor
    protected CountryCode() {
        this.code = null;
    }

    /**
     * Erstellt einen Ländercode
     * 
     * @param code ISO 3166-1 Alpha-2 Code (z.B. "DE", "US", "FR")
     */
    public CountryCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Ländercode darf nicht leer sein");
        }
        
        String normalizedCode = code.trim().toUpperCase();
        
        if (!COUNTRY_CODE_PATTERN.matcher(normalizedCode).matches()) {
            throw new IllegalArgumentException(
                "Ungültiger Ländercode: " + code + " (Muss 2 Großbuchstaben sein, z.B. DE, US, FR)"
            );
        }
        
        this.code = normalizedCode;
    }

    /**
     * Prüft ob das Land zur EU gehört
     */
    public boolean isEuropeanUnion() {
        // EU-27 Länder (Stand 2025)
        return switch (code) {
            case "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
                 "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL",
                 "PL", "PT", "RO", "SK", "SI", "ES", "SE" -> true;
            default -> false;
        };
    }

    /**
     * Prüft ob Visa-Anforderungen relevant sind (nicht-EU)
     */
    public boolean requiresVisaCheck() {
        return !isEuropeanUnion();
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryCode that = (CountryCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}

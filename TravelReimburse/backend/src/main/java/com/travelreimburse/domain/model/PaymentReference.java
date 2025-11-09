package com.travelreimburse.domain.model;

import jakarta.persistence.Embeddable;
import java.util.UUID;

/**
 * Value Object für eindeutige Zahlungsreferenzen.
 * Immutable - repräsentiert eine eindeutige Referenz für Zahlungen in EasyPay.
 */
@Embeddable
public class PaymentReference {

    private String reference;

    protected PaymentReference() {
        // JPA
    }

    private PaymentReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Zahlungsreferenz darf nicht null oder leer sein");
        }
        if (reference.length() > 50) {
            throw new IllegalArgumentException("Zahlungsreferenz darf nicht länger als 50 Zeichen sein");
        }
        this.reference = reference;
    }

    /**
     * Generiert eine neue eindeutige Zahlungsreferenz
     */
    public static PaymentReference generate() {
        String generated = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentReference(generated);
    }

    /**
     * Erstellt eine Zahlungsreferenz aus einem String
     */
    public static PaymentReference of(String reference) {
        return new PaymentReference(reference);
    }

    /**
     * Gibt die Referenz zurück
     */
    public String getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentReference)) return false;
        PaymentReference that = (PaymentReference) o;
        return reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return reference.hashCode();
    }
}


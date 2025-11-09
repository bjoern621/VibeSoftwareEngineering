package com.travelreimburse.domain.model;

/**
 * Enum für Zahlungsstatus.
 * Repräsentiert die verschiedenen Zustände einer Zahlung im EasyPay-System.
 */
public enum PaymentStatus {
    PENDING("Ausstehend"),
    SUBMITTED_TO_EASYPAY("An EasyPay übermittelt"),
    PROCESSING("Wird bearbeitet"),
    SUCCESS("Erfolgreich"),
    FAILED("Fehlgeschlagen"),
    REJECTED("Abgelehnt");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


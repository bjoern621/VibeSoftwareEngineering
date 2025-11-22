package com.rentacar.domain.model;

/**
 * Status einer Buchung im System.
 * 
 * Definiert den Lebenszyklus einer Buchung von der Anfrage bis zum Abschluss.
 */
public enum BookingStatus {
    /** Buchung angefragt, wartet auf Bestätigung */
    REQUESTED,
    
    /** Buchung bestätigt */
    CONFIRMED,
    
    /** Buchung storniert */
    CANCELLED,
    
    /** Buchung abgelaufen (nicht abgeholt) */
    EXPIRED,
    
    /** Buchung abgeschlossen (Fahrzeug zurückgegeben) */
    COMPLETED;

    /**
     * Prüft, ob der Status stornierbar ist.
     * 
     * @return true wenn Buchung stornierbar ist
     */
    public boolean isCancellable() {
        return this == REQUESTED || this == CONFIRMED;
    }

    /**
     * Prüft, ob der Status als "aktiv" gilt.
     * 
     * @return true wenn Buchung aktiv ist
     */
    public boolean isActive() {
        return this == REQUESTED || this == CONFIRMED;
    }

    /**
     * Prüft, ob der Status als "abgeschlossen" gilt.
     * 
     * @return true wenn Buchung abgeschlossen ist
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == CANCELLED || this == EXPIRED;
    }
}

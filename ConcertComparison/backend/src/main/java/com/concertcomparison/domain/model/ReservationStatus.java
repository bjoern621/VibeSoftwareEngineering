package com.concertcomparison.domain.model;

/**
 * Status einer Reservation (Hold).
 */
public enum ReservationStatus {
    /**
     * Reservation ist aktiv und noch nicht abgelaufen.
     */
    ACTIVE,
    
    /**
     * Reservation ist abgelaufen (TTL überschritten).
     * Scheduler löscht expired Reservations + released Seats.
     */
    EXPIRED,
    
    /**
     * Reservation wurde in eine Order umgewandelt (Seat purchased).
     */
    PURCHASED
}

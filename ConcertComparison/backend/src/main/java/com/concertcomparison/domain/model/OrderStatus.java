package com.concertcomparison.domain.model;

/**
 * Status eines Ticket-Kaufs (Order).
 * 
 * <p>Definiert die möglichen Zustände einer Order:</p>
 * <ul>
 *   <li>CONFIRMED - Order wurde erfolgreich abgeschlossen</li>
 * </ul>
 */
public enum OrderStatus {
    /**
     * Order wurde erfolgreich abgeschlossen und bestätigt.
     */
    CONFIRMED
}

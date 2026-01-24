package com.concertcomparison.domain.exception;

/**
 * Domain Exception: Wird geworfen, wenn eine Order nicht gefunden wird.
 * 
 * Anwendungsfall:
 * - User versucht, Details einer nicht-existierenden Order abzurufen
 * - Versuch, eine nicht-existierende Order zu aktualisieren
 */
public class OrderNotFoundException extends ResourceNotFoundException {
    
    /**
     * Erstellt eine neue OrderNotFoundException mit der angegebenen Order-ID.
     * 
     * @param orderId ID der nicht gefundenen Order
     */
    public OrderNotFoundException(Long orderId) {
        super(String.format("Die Bestellung mit ID %d wurde nicht gefunden", orderId), "Order", orderId.toString());
    }

    /**
     * Erstellt eine neue OrderNotFoundException mit custom Fehlermeldung.
     * 
     * @param message Benutzerfreundliche Fehlermeldung
     */
    public OrderNotFoundException(String message) {
        super(message, "Order", null);
    }
}

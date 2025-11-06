package com.travelreimburse.application.service;

/**
 * Exception für nicht gefundene Belege.
 */
public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(Long id) {
        super("Beleg mit ID " + id + " nicht gefunden");
    }

    public ReceiptNotFoundException(String message) {
        super(message);
    }
}


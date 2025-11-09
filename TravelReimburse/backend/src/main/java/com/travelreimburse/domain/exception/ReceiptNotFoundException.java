package com.travelreimburse.domain.exception;

/**
 * Domain Exception für nicht gefundene Belege.
 * DDD: Domain-spezifische Exception im Domain Layer.
 */
public class ReceiptNotFoundException extends RuntimeException {

    private final Long receiptId;

    public ReceiptNotFoundException(Long id) {
        super("Beleg mit ID " + id + " nicht gefunden");
        this.receiptId = id;
    }

    public ReceiptNotFoundException(Long id, String additionalMessage) {
        super("Beleg mit ID " + id + " nicht gefunden: " + additionalMessage);
        this.receiptId = id;
    }

    public Long getReceiptId() {
        return receiptId;
    }
}


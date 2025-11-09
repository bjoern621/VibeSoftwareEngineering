package com.travelreimburse.domain.exception;

import com.travelreimburse.domain.model.ReceiptStatus;

/**
 * Domain Exception für ungültige Beleg-Zustandsänderungen.
 * DDD: Spezifische Domain Exception statt generischer IllegalStateException.
 */
public class InvalidReceiptStateException extends RuntimeException {

    private final Long receiptId;
    private final ReceiptStatus currentStatus;
    private final String attemptedAction;

    public InvalidReceiptStateException(Long receiptId, ReceiptStatus currentStatus, String attemptedAction) {
        super(String.format("Beleg %d im Status %s kann nicht %s werden",
                receiptId, currentStatus, attemptedAction));
        this.receiptId = receiptId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public InvalidReceiptStateException(String message, Long receiptId, ReceiptStatus currentStatus) {
        super(message);
        this.receiptId = receiptId;
        this.currentStatus = currentStatus;
        this.attemptedAction = null;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public ReceiptStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}


package com.travelreimburse.domain.event;

import com.travelreimburse.domain.model.Receipt;
import com.travelreimburse.domain.model.ReceiptStatus;

/**
 * Domain Event für Statusänderungen von Belegen
 * Wird publiziert, wenn sich der Status eines Belegs ändert
 */
public class ReceiptStatusChangedEvent {
    private final Receipt receipt;
    private final ReceiptStatus oldStatus;
    private final ReceiptStatus newStatus;

    public ReceiptStatusChangedEvent(Receipt receipt,
                                     ReceiptStatus oldStatus,
                                     ReceiptStatus newStatus) {
        this.receipt = receipt;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public ReceiptStatus getOldStatus() {
        return oldStatus;
    }

    public ReceiptStatus getNewStatus() {
        return newStatus;
    }
}


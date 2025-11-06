package com.travelreimburse.domain.model;

/**
 * Status eines Belegs im Lifecycle.
 */
public enum ReceiptStatus {
    UPLOADED("Hochgeladen"),
    VALIDATED("Validiert"),
    REJECTED("Abgelehnt"),
    ARCHIVED("Archiviert");

    private final String displayName;

    ReceiptStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


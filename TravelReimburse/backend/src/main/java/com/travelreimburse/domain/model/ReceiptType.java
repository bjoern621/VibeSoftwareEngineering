package com.travelreimburse.domain.model;

/**
 * Enum für verschiedene Belegtypen im System.
 * Definiert alle unterstützten Kategorien von Belegen.
 */
public enum ReceiptType {
    HOTEL("Hotelrechnung"),
    FLIGHT("Flugticket"),
    TRAIN("Bahnticket"),
    TAXI("Taxiquittung"),
    CAR_RENTAL("Mietwagenbeleg"),
    FUEL("Tankbeleg"),
    PARKING("Parkgebühr"),
    MEAL("Verpflegungsbeleg"),
    CONFERENCE("Konferenzgebühr"),
    OTHER("Sonstiges");

    private final String displayName;

    ReceiptType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


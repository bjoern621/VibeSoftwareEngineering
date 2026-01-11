package com.concertcomparison.domain.model;

/**
 * Status eines Sitzplatzes im Concert Comparison System.
 * 
 * Definiert die möglichen Zustände eines Seats während des Ticket-Verkaufsprozesses:
 * - AVAILABLE: Seat ist verfügbar und kann reserviert werden
 * - HELD: Seat ist temporär reserviert (TTL-basiert)
 * - SOLD: Seat wurde verkauft (finaler Zustand)
 */
public enum SeatStatus {
    
    /**
     * Seat ist verfügbar und kann reserviert werden.
     */
    AVAILABLE("Verfügbar"),
    
    /**
     * Seat ist temporär reserviert (Hold mit TTL).
     * Wird automatisch freigegeben nach Ablauf der Hold-Zeit.
     */
    HELD("Reserviert"),
    
    /**
     * Seat wurde verkauft (finaler Zustand, nicht reversibel).
     */
    SOLD("Verkauft");
    
    private final String displayName;
    
    SeatStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Liefert den deutschen Anzeigenamen für den Status.
     * 
     * @return Anzeigename (z.B. "Verfügbar", "Reserviert", "Verkauft")
     */
    public String getDisplayName() {
        return displayName;
    }
}

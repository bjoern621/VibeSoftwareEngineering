package com.concertcomparison.domain.event;

import com.concertcomparison.domain.model.SeatStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Event - wird gepublisht wenn sich der Status eines Seats ändert.
 * 
 * Dieses Event triggert automatische Cache-Invalidierung für die
 * Seat-Verfügbarkeit des betroffenen Concerts.
 * 
 * Use Cases:
 * - Hold erstellt (AVAILABLE → HELD)
 * - Hold storniert (HELD → AVAILABLE)
 * - Ticket gekauft (HELD → SOLD)
 * - Hold abgelaufen (HELD → AVAILABLE - via Scheduler)
 * 
 * DDD Value Object Pattern:
 * - Immutable (alle Felder final)
 * - Keine Business Logic
 * - Framework-agnostisch (keine Spring Annotations)
 */
public final class SeatStatusChangedEvent {
    
    private final Long seatId;
    private final Long concertId;
    private final SeatStatus oldStatus;
    private final SeatStatus newStatus;
    private final LocalDateTime timestamp;
    private final String userId;
    private final String reason;
    
    /**
     * Erstellt ein neues SeatStatusChangedEvent.
     * 
     * @param seatId ID des betroffenen Seats
     * @param concertId ID des Concerts (für Cache-Invalidierung)
     * @param oldStatus Vorheriger Status
     * @param newStatus Neuer Status
     * @param userId ID des Users der die Änderung ausgelöst hat (optional)
     * @param reason Grund für die Änderung (z.B. "HOLD_CREATED", "TICKET_PURCHASED")
     */
    public SeatStatusChangedEvent(
            Long seatId,
            Long concertId,
            SeatStatus oldStatus,
            SeatStatus newStatus,
            String userId,
            String reason) {
        
        if (seatId == null) {
            throw new IllegalArgumentException("SeatId darf nicht null sein");
        }
        if (concertId == null) {
            throw new IllegalArgumentException("ConcertId darf nicht null sein");
        }
        if (oldStatus == null) {
            throw new IllegalArgumentException("OldStatus darf nicht null sein");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("NewStatus darf nicht null sein");
        }
        
        this.seatId = seatId;
        this.concertId = concertId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.reason = reason;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Factory Method: Hold erstellt (AVAILABLE → HELD).
     */
    public static SeatStatusChangedEvent holdCreated(Long seatId, Long concertId, String userId) {
        return new SeatStatusChangedEvent(
            seatId, 
            concertId, 
            SeatStatus.AVAILABLE, 
            SeatStatus.HELD,
            userId,
            "HOLD_CREATED"
        );
    }
    
    /**
     * Factory Method: Hold storniert (HELD → AVAILABLE).
     */
    public static SeatStatusChangedEvent holdCancelled(Long seatId, Long concertId, String userId) {
        return new SeatStatusChangedEvent(
            seatId, 
            concertId, 
            SeatStatus.HELD, 
            SeatStatus.AVAILABLE,
            userId,
            "HOLD_CANCELLED"
        );
    }
    
    /**
     * Factory Method: Hold abgelaufen (HELD → AVAILABLE).
     */
    public static SeatStatusChangedEvent holdExpired(Long seatId, Long concertId) {
        return new SeatStatusChangedEvent(
            seatId, 
            concertId, 
            SeatStatus.HELD, 
            SeatStatus.AVAILABLE,
            null,
            "HOLD_EXPIRED"
        );
    }
    
    /**
     * Factory Method: Ticket gekauft (HELD → SOLD).
     */
    public static SeatStatusChangedEvent ticketPurchased(Long seatId, Long concertId, String userId) {
        return new SeatStatusChangedEvent(
            seatId, 
            concertId, 
            SeatStatus.HELD, 
            SeatStatus.SOLD,
            userId,
            "TICKET_PURCHASED"
        );
    }
    
    // ==================== GETTERS ====================
    
    public Long getSeatId() {
        return seatId;
    }
    
    public Long getConcertId() {
        return concertId;
    }
    
    public SeatStatus getOldStatus() {
        return oldStatus;
    }
    
    public SeatStatus getNewStatus() {
        return newStatus;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getReason() {
        return reason;
    }
    
    // ==================== OBJECT METHODS ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatStatusChangedEvent that = (SeatStatusChangedEvent) o;
        return Objects.equals(seatId, that.seatId) &&
               Objects.equals(concertId, that.concertId) &&
               oldStatus == that.oldStatus &&
               newStatus == that.newStatus &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(reason, that.reason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(seatId, concertId, oldStatus, newStatus, timestamp, userId, reason);
    }
    
    @Override
    public String toString() {
        return String.format(
            "SeatStatusChangedEvent[seatId=%d, concertId=%d, %s→%s, reason=%s, userId=%s, timestamp=%s]",
            seatId, concertId, oldStatus, newStatus, reason, userId, timestamp
        );
    }
}

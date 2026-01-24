package com.concertcomparison.domain.model;

import com.concertcomparison.domain.exception.SeatNotAvailableException;
import com.concertcomparison.domain.exception.SeatNotHeldException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Seat Entity - Repräsentiert einen Sitzplatz für ein Konzert.
 * 
 * DDD Aggregate Root mit Business Logic für Seat-Lifecycle:
 * - Reservierung (hold) mit TTL
 * - Verkauf (sell) nach erfolgreicher Reservierung
 * - Freigabe (releaseHold) bei Timeout oder Stornierung
 * 
 * Concurrency Control:
 * - Optimistic Locking via @Version für Race Condition Prevention
 * - Business Invariants werden in Entity-Methoden validiert
 * 
 * State Transitions:
 * AVAILABLE → HELD (via hold())
 * HELD → SOLD (via sell())
 * HELD → AVAILABLE (via releaseHold())
 */
@Entity
@Table(name = "seats", indexes = {
    @Index(name = "idx_concert_id", columnList = "concert_id"),
    @Index(name = "idx_concert_status", columnList = "concert_id, status"),
    @Index(name = "idx_hold_expires", columnList = "hold_expires_at")
})
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "concert_id", nullable = false)
    private Long concertId;
    
    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "block", nullable = false, length = 50)
    private String block;
    
    @Column(name = "seat_row", nullable = false, length = 10)
    private String row;
    
    @Column(name = "number", nullable = false, length = 10)
    private String number;
    
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatStatus status;
    
    @Column(name = "hold_reservation_id", length = 100)
    private String holdReservationId;
    
    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;
    
    /**
     * Optimistic Locking - verhindert Lost Updates bei konkurrierenden Transaktionen.
     * Wird von JPA automatisch bei jedem Update inkrementiert.
     */
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Default Constructor (JPA required).
     */
    protected Seat() {
        // Für JPA/Hibernate
    }
    
    /**
     * Business Constructor - erstellt einen neuen verfügbaren Seat.
     * 
     * @param concertId ID des Konzerts
     * @param seatNumber Sitzplatznummer (z.B. "A-12", "VIP-001")
     * @param category Kategorie (z.B. "VIP", "CATEGORY_A")
     * @param block Block/Bereich (z.B. "Block A", "Standing Area 1")
     * @param row Reihe (z.B. "1", "A")
     * @param number Sitznummer in der Reihe (z.B. "1", "12")
     * @param price Preis in Euro
     */
    public Seat(Long concertId, String seatNumber, String category, String block, String row, String number, Double price) {
        validateConstructorParameters(concertId, seatNumber, category, block, row, number, price);
        
        this.concertId = concertId;
        this.seatNumber = seatNumber;
        this.category = category;
        this.block = block;
        this.row = row;
        this.number = number;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * Reserviert den Seat für eine bestimmte Zeit (Hold mit TTL).
     * 
     * Business Rules:
     * - Seat muss AVAILABLE sein
     * - Nur ein Hold gleichzeitig möglich
     * - Hold hat automatische Ablaufzeit (TTL)
     * 
     * @param reservationId Eindeutige Reservierungs-ID
     * @param holdDurationMinutes Dauer der Reservierung in Minuten
     * @throws SeatNotAvailableException wenn Seat nicht verfügbar ist
     */
    public void hold(String reservationId, int holdDurationMinutes) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation-ID darf nicht leer sein");
        }
        
        if (holdDurationMinutes <= 0) {
            throw new IllegalArgumentException("Hold-Dauer muss positiv sein");
        }
        
        if (this.status != SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException(
                String.format("Seat %s (ID: %d) ist bereits %s und kann nicht reserviert werden", 
                    seatNumber, id, status.getDisplayName())
            );
        }
        
        this.status = SeatStatus.HELD;
        this.holdReservationId = reservationId;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(holdDurationMinutes);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Reserviert den Seat mit direktem Ablaufzeitpunkt (für Tests).
     * 
     * @param reservationId Eindeutige Reservierungs-ID
     * @param expiresAt Ablaufzeitpunkt der Reservierung
     * @throws SeatNotAvailableException wenn Seat nicht verfügbar ist
     */
    public void hold(String reservationId, LocalDateTime expiresAt) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation-ID darf nicht leer sein");
        }
        
        if (expiresAt == null) {
            throw new IllegalArgumentException("Ablaufzeitpunkt darf nicht null sein");
        }
        
        if (this.status != SeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException(
                String.format("Seat %s (ID: %d) ist bereits %s und kann nicht reserviert werden", 
                    seatNumber, id, status.getDisplayName())
            );
        }
        
        this.status = SeatStatus.HELD;
        this.holdReservationId = reservationId;
        this.holdExpiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verkauft den Seat (finale Transaktion).
     * 
     * Business Rules:
     * - Seat muss HELD sein (vorherige Reservierung erforderlich)
     * - Verkauf ist final und nicht reversibel
     * - Hold-Informationen werden entfernt
     * 
     * @throws SeatNotHeldException wenn Seat nicht reserviert ist
     */
    public void sell() {
        if (this.status != SeatStatus.HELD) {
            throw new SeatNotHeldException(
                String.format("Seat %s (ID: %d) muss zuerst reserviert werden. Aktueller Status: %s", 
                    seatNumber, id, status.getDisplayName())
            );
        }
        
        this.status = SeatStatus.SOLD;
        this.holdReservationId = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verkauft den Seat mit Validierung der Reservierungs-ID.
     * 
     * Business Rules:
     * - Seat muss HELD sein
     * - Reservierungs-ID muss übereinstimmen
     * - Verkauf ist final
     * 
     * @param reservationId Reservierungs-ID zur Validierung
     * @throws IllegalStateException wenn Seat nicht HELD ist oder ID nicht passt
     */
    public void sell(String reservationId) {
        if (this.status != SeatStatus.HELD) {
            throw new IllegalStateException(
                String.format("Seat %s (ID: %d) ist nicht reserviert. Status: %s", 
                    seatNumber, id, status.getDisplayName())
            );
        }
        
        if (!Objects.equals(this.holdReservationId, reservationId)) {
            throw new IllegalStateException(
                String.format("Seat %s (ID: %d) hat eine andere Reservierungs-ID", 
                    seatNumber, id)
            );
        }
        
        this.status = SeatStatus.SOLD;
        this.holdReservationId = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Gibt eine Reservierung frei (Stornierung oder Timeout).
     * 
     * Business Rules:
     * - Seat muss HELD sein
     * - Seat wird wieder AVAILABLE
     * - Hold-Informationen werden entfernt
     * 
     * @throws IllegalStateException wenn Seat nicht reserviert ist
     */
    public void releaseHold() {
        if (this.status != SeatStatus.HELD) {
            throw new IllegalStateException(
                String.format("Seat %s (ID: %d) ist nicht reserviert und kann nicht freigegeben werden. Status: %s", 
                    seatNumber, id, status.getDisplayName())
            );
        }
        
        this.status = SeatStatus.AVAILABLE;
        this.holdReservationId = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Setzt einen verkauften Seat zurück auf HELD Status (Payment Rollback).
     * 
     * Business Rules:
     * - Seat muss SOLD sein
     * - Wird bei fehlgeschlagener Zahlung aufgerufen
     * - Seat wird mit neuer Reservation-ID reserviert
     * - Gibt dem User eine zweite Chance für Payment
     * 
     * @param reservationId Neue Reservierungs-ID für Re-Payment
     * @param expiresAt Ablaufzeitpunkt der neuen Reservierung
     * @throws IllegalStateException wenn Seat nicht SOLD ist
     * @throws IllegalArgumentException bei ungültigen Parametern
     */
    public void rollbackToHeld(String reservationId, LocalDateTime expiresAt) {
        if (this.status != SeatStatus.SOLD) {
            throw new IllegalStateException(
                String.format("Nur SOLD Seats können auf HELD zurückgesetzt werden (aktuell: %s)", 
                    this.status.getDisplayName())
            );
        }
        
        Objects.requireNonNull(reservationId, "ReservationId darf nicht null sein");
        Objects.requireNonNull(expiresAt, "ExpiresAt darf nicht null sein");
        
        if (reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("ReservationId darf nicht leer sein");
        }
        
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("ExpiresAt darf nicht in der Vergangenheit liegen");
        }
        
        this.status = SeatStatus.HELD;
        this.holdReservationId = reservationId;
        this.holdExpiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prüft, ob die Reservierung abgelaufen ist.
     * 
     * @return true wenn Seat HELD ist und Hold-Zeit abgelaufen ist
     */
    public boolean isHoldExpired() {
        return status == SeatStatus.HELD 
            && holdExpiresAt != null 
            && LocalDateTime.now().isAfter(holdExpiresAt);
    }

    /**
     * Aktualisiert die holdReservationId (wird nach Reservation-Erstellung aufgerufen).
     * Nur erlaubt für Seats im Status HELD.
     */
    public void updateHoldReservationId(String reservationId) {
        if (this.status != SeatStatus.HELD) {
            throw new IllegalStateException("Nur HELD Seats können holdReservationId updaten");
        }
        this.holdReservationId = reservationId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prüft, ob der Seat derzeit verfügbar ist.
     * 
     * @return true wenn Status AVAILABLE ist
     */
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }
    
    /**
     * Prüft, ob der Seat verkauft ist.
     * 
     * @return true wenn Status SOLD ist
     */
    public boolean isSold() {
        return status == SeatStatus.SOLD;
    }
    
    /**
     * Prüft, ob der Seat reserviert ist.
     * 
     * @return true wenn Status HELD ist
     */
    public boolean isHeld() {
        return status == SeatStatus.HELD;
    }
    
    // ==================== VALIDATION ====================
    
    private void validateConstructorParameters(Long concertId, String seatNumber, 
                                               String category, String block,
                                               String row, String number, Double price) {
        if (concertId == null) {
            throw new IllegalArgumentException("Concert-ID darf nicht null sein");
        }
        
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Sitzplatznummer darf nicht leer sein");
        }
        
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Kategorie darf nicht leer sein");
        }
        
        if (block == null || block.trim().isEmpty()) {
            throw new IllegalArgumentException("Block darf nicht leer sein");
        }
        
        if (row == null || row.trim().isEmpty()) {
            throw new IllegalArgumentException("Reihe darf nicht leer sein");
        }
        
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Sitznummer darf nicht leer sein");
        }
        
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Preis muss positiv sein");
        }
    }
    
    // ==================== LIFECYCLE CALLBACKS ====================
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = SeatStatus.AVAILABLE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    
    public Long getId() {
        return id;
    }
    
    public Long getConcertId() {
        return concertId;
    }
    
    public String getSeatNumber() {
        return seatNumber;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getBlock() {
        return block;
    }
    
    public String getRow() {
        return row;
    }
    
    public String getNumber() {
        return number;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public SeatStatus getStatus() {
        return status;
    }
    
    public String getHoldReservationId() {
        return holdReservationId;
    }
    
    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Setzt die ID (nur für Tests).
     * 
     * @param id Die zu setzende ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Seat{id=%d, concertId=%d, seatNumber='%s', category='%s', status=%s}", 
            id, concertId, seatNumber, category, status);
    }
}

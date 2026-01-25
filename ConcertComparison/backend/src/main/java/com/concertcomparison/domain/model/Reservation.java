package com.concertcomparison.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reservation Aggregate Root (Hold).
 * Repräsentiert eine zeitlich begrenzte Reservierung eines Seats.
 * DDD: Enthält Business Logic für Hold-Lifecycle (create, expire, purchase).
 */
@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservation_seat", columnList = "seat_id"),
    @Index(name = "idx_reservation_user", columnList = "user_id"),
    @Index(name = "idx_reservation_expires", columnList = "expires_at"),
    @Index(name = "idx_reservation_status", columnList = "status")
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID des reservierten Seats.
     * Business Rule: Ein Seat kann maximal eine ACTIVE Reservation haben.
     */
    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    /**
     * ID des Users, der den Hold hält.
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * Ablaufzeitpunkt der Reservation.
     * Nach diesem Zeitpunkt ist die Reservation expired und kann gelöscht werden.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Status der Reservation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Optimistic Locking für Concurrency Control.
     * Verhindert, dass zwei parallele Requests denselben Hold löschen/kaufen.
     */
    @Version
    private Long version;

    // ======================== Constructors ========================

    protected Reservation() {
        // JPA only
    }

    /**
     * Factory Method: Erstellt eine neue ACTIVE Reservation.
     * 
     * @param seatId ID des zu reservierenden Seats
     * @param userId ID des Users
     * @param ttlMinutes TTL in Minuten
     * @return Neue Reservation im Status ACTIVE
     */
    public static Reservation createHold(Long seatId, String userId, int ttlMinutes) {
        Objects.requireNonNull(seatId, "SeatId darf nicht null sein");
        Objects.requireNonNull(userId, "UserId darf nicht null sein");
        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException("TTL muss positiv sein");
        }

        Reservation reservation = new Reservation();
        reservation.seatId = seatId;
        reservation.userId = userId;
        reservation.status = ReservationStatus.ACTIVE;
        reservation.createdAt = LocalDateTime.now();
        reservation.updatedAt = LocalDateTime.now();
        reservation.expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);
        
        return reservation;
    }

    // ======================== Business Methods ========================

    /**
     * Markiert die Reservation als EXPIRED.
     * Business Rule: Nur ACTIVE Reservations können expiren.
     */
    public void expire() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException(
                String.format("Nur ACTIVE Reservations können expiren (aktuell: %s)", this.status)
            );
        }
        this.status = ReservationStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Markiert die Reservation als PURCHASED (Hold wurde in Order umgewandelt).
     * Business Rule: Nur ACTIVE Reservations können purchased werden.
     */
    public void markAsPurchased() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException(
                String.format("Nur ACTIVE Reservations können purchased werden (aktuell: %s)", this.status)
            );
        }
        this.status = ReservationStatus.PURCHASED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Prüft, ob die Reservation abgelaufen ist.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Prüft, ob die Reservation aktiv ist.
     */
    public boolean isActive() {
        return this.status == ReservationStatus.ACTIVE && !isExpired();
    }

    // ======================== Getters ========================

    public Long getId() {
        return id;
    }

    public Long getSeatId() {
        return seatId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    // ======================== Business Methods ========================

    /**
     * Lässt die Reservation sofort ablaufen (für Tests).
     * Setzt expiresAt auf jetzt - 1 Minute.
     */
    public void expireNow() {
        this.expiresAt = LocalDateTime.now().minusMinutes(1);
    }

    // ======================== Lifecycle Callbacks ========================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ======================== Equals & HashCode ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", seatId=" + seatId +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", expiresAt=" + expiresAt +
                '}';
    }
}

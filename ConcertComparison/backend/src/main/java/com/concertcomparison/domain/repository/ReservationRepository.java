package com.concertcomparison.domain.repository;

import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Reservations (Holds).
 * DDD: Port im Domain Layer, Adapter im Infrastructure Layer.
 */
public interface ReservationRepository {

    /**
     * Findet eine Reservation by ID.
     */
    Optional<Reservation> findById(Long id);

    /**
     * Findet die aktive Reservation für einen Seat (falls vorhanden).
     * Business Rule: Ein Seat kann maximal eine ACTIVE Reservation haben.
     */
    Optional<Reservation> findActiveBySeatId(Long seatId);

    /**
     * Findet alle aktiven Reservations eines Users.
     */
    List<Reservation> findActiveByUserId(String userId);

    /**
     * Findet alle Reservations mit einem bestimmten Status.
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Findet alle abgelaufenen Reservations (expiresAt < now AND status = ACTIVE).
     * Wird vom Cleanup-Scheduler verwendet.
     */
    List<Reservation> findExpired(LocalDateTime now);

    /**
     * Speichert eine Reservation (insert oder update).
     */
    Reservation save(Reservation reservation);

    /**
     * Löscht eine Reservation.
     */
    void delete(Reservation reservation);

    /**
     * Löscht eine Reservation by ID.
     */
    void deleteById(Long id);

    /**
     * Zählt aktive Reservations für einen User.
     */
    long countActiveByUserId(String userId);
}

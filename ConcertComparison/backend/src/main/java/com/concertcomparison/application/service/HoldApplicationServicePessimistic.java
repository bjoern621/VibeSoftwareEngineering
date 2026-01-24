package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.SeatNotFoundException;
import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.HoldResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * High-Performance Hold Service mit Pessimistic Locking.
 * 
 * VERWENDUNG: Für kritische High-Traffic Szenarien (> 10.000 req/s auf populäre Seats).
 * 
 * Unterschiede zum Standard HoldApplicationService:
 * - Nutzt PESSIMISTIC WRITE LOCK (SELECT ... FOR UPDATE)
 * - Keine Optimistic Lock Exceptions
 * - Serialisierung der Requests auf denselben Seat
 * - Höhere Latenz pro Request, aber garantierter Erfolg
 * 
 * TRADE-OFF:
 * - Optimistic Locking (Standard): Hoher Durchsatz, viele Retries bei Konflikten
 * - Pessimistic Locking (Diese Klasse): Niedrigerer Durchsatz, keine Retries
 * 
 * WANN NUTZEN:
 * - Ticket-Verkaufsstart für Taylor Swift / Beyoncé (Hot Seats)
 * - Flash Sales / Rabattaktionen
 * - Limited Edition Produkte
 * 
 * WANN NICHT:
 * - Normale Auslastung (< 1000 req/s)
 * - Gleichmäßige Verteilung auf viele Seats
 * - Read-Heavy Workloads
 */
@Service
public class HoldApplicationServicePessimistic {

    private static final Logger logger = LoggerFactory.getLogger(HoldApplicationServicePessimistic.class);

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    @Value("${concert.hold.ttl-minutes:15}")
    private int holdTtlMinutes;

    public HoldApplicationServicePessimistic(
            SeatRepository seatRepository,
            ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Erstellt einen Hold mit Pessimistic Locking (FOR UPDATE).
     * 
     * ⚠️ HIGH-PERFORMANCE MODE ⚠️
     * 
     * Flow:
     * 1. Seat mit DB-Lock laden (SELECT ... FOR UPDATE)
     * 2. Business Rules prüfen (nur wenn Lock erfolgreich)
     * 3. Hold erstellen
     * 4. Lock wird mit Transaction-Ende freigegeben
     * 
     * Garantien:
     * - ✅ Kein Optimistic Lock Exception
     * - ✅ Maximal 1 Request gleichzeitig pro Seat
     * - ✅ ACID-Garantien
     * 
     * Nachteile:
     * - ⏱️ Höhere Latenz (Requests warten auf Lock)
     * - 📉 Niedrigerer Durchsatz bei Hot Seats
     * 
     * @param seatId ID des Seats
     * @param userId ID des Users
     * @return HoldResponseDTO
     * @throws IllegalArgumentException wenn Seat nicht existiert
     * @throws IllegalStateException wenn Seat nicht verfügbar
     */
    @Transactional
    public HoldResponseDTO createHoldWithPessimisticLock(Long seatId, String userId) {
        logger.info("[PESSIMISTIC] Creating hold for seatId={}, userId={}", seatId, userId);

        // 1. Seat mit Pessimistic Lock laden
        // → DB führt SELECT ... FOR UPDATE aus
        // → Andere Transaktionen warten bis Lock frei ist
        Seat seat = seatRepository.findByIdForUpdate(seatId)
            .orElseThrow(() -> new SeatNotFoundException(seatId));

        logger.debug("[PESSIMISTIC] Seat {} locked, status: {}", seatId, seat.getStatus());

        // 2. Prüfen ob bereits Hold existiert
        reservationRepository.findActiveBySeatId(seatId).ifPresent(existingHold -> {
            throw new IllegalStateException(
                String.format("Seat %d hat bereits einen aktiven Hold (holdId=%d)", 
                    seatId, existingHold.getId())
            );
        });

        // 3. Seat auf HELD setzen (Domain Logic)
        // → Kein Conflict möglich, da wir den Lock haben!
        seat.hold(String.valueOf(seatId), holdTtlMinutes);
        seatRepository.save(seat);

        // 4. Reservation erstellen
        Reservation reservation = Reservation.createHold(seatId, userId, holdTtlMinutes);
        reservation = reservationRepository.save(reservation);

        // 5. Seat holdReservationId aktualisieren
        seat.updateHoldReservationId(String.valueOf(reservation.getId()));
        seatRepository.save(seat);

        logger.info("[PESSIMISTIC] Hold created: holdId={}, seatId={}, expiresAt={}", 
            reservation.getId(), seatId, reservation.getExpiresAt());

        // 6. Response DTO erstellen
        int ttlSeconds = (int) Duration.ofMinutes(holdTtlMinutes).getSeconds();
        return new HoldResponseDTO(
            String.valueOf(reservation.getId()),
            String.valueOf(seatId),
            ttlSeconds,
            reservation.getExpiresAt()
        );
        
        // 7. Lock wird beim Transaction Commit automatisch freigegeben
    }

    /**
     * Storniert einen Hold mit Pessimistic Locking.
     * 
     * @param holdId ID des Holds
     */
    @Transactional
    public void releaseHoldWithPessimisticLock(Long holdId) {
        logger.info("[PESSIMISTIC] Releasing hold: holdId={}", holdId);

        Reservation reservation = reservationRepository.findById(holdId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Hold mit ID %d nicht gefunden", holdId)
            ));

        if (!reservation.isActive()) {
            throw new IllegalStateException(
                String.format("Hold %d ist nicht aktiv (status=%s)", 
                    holdId, reservation.getStatus())
            );
        }

        // Seat mit Lock laden
        Seat seat = seatRepository.findByIdForUpdate(reservation.getSeatId())
            .orElseThrow(() -> new IllegalStateException(
                String.format("Seat %d für Hold %d nicht gefunden", 
                    reservation.getSeatId(), holdId)
            ));

        // Seat freigeben
        seat.releaseHold();
        seatRepository.save(seat);

        // Reservation löschen
        reservationRepository.delete(reservation);

        logger.info("[PESSIMISTIC] Hold released: holdId={}, seatId={}", holdId, reservation.getSeatId());
    }
}

package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.ReservationNotFoundException;
import com.concertcomparison.domain.exception.SeatNotAvailableException;
import com.concertcomparison.domain.exception.SeatNotFoundException;
import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.HoldResponseDTO;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Application Service für Hold-Operationen (US-02).
 * Orchestriert Domain Entities (Seat, Reservation) und Repositories.
 */
@Service
public class HoldApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(HoldApplicationService.class);

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${concert.hold.ttl-minutes:15}")
    private int holdTtlMinutes;

    public HoldApplicationService(
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            ApplicationEventPublisher eventPublisher) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Erstellt einen Hold für einen Seat.
     * 
     * Business Rules:
     * - Seat muss existieren
     * - Seat muss AVAILABLE sein
     * - Kein anderer aktiver Hold für diesen Seat
     * 
     * @param seatId ID des Seats
     * @param userId ID des Users
     * @return HoldResponseDTO mit holdId, seatId, ttlSeconds, expiresAt
     * @throws IllegalArgumentException wenn Seat nicht existiert
     * @throws IllegalStateException wenn Seat nicht AVAILABLE ist
     * @throws OptimisticLockException bei Concurrency Conflict
     */
    @Transactional
    public HoldResponseDTO createHold(Long seatId, String userId) {
        logger.info("Creating hold for seatId={}, userId={}", seatId, userId);

        // 1. Seat laden
        Seat seat = seatRepository.findById(seatId)
            .orElseThrow(() -> new SeatNotFoundException(seatId));

        // 2. Prüfen ob bereits ein aktiver Hold existiert
        reservationRepository.findActiveBySeatId(seatId).ifPresent(existingHold -> {
            throw new SeatNotAvailableException(
                String.format("Seat %d hat bereits einen aktiven Hold (holdId=%d)", 
                    seatId, existingHold.getId())
            );
        });

        // 3. Seat auf HELD setzen (Domain Logic)
        seat.hold(String.valueOf(seatId), holdTtlMinutes);
        seatRepository.save(seat);

        // 4. Reservation erstellen
        Reservation reservation = Reservation.createHold(seatId, userId, holdTtlMinutes);
        reservation = reservationRepository.save(reservation);

        // 5. Seat holdReservationId mit finaler Reservation ID aktualisieren
        seat.updateHoldReservationId(String.valueOf(reservation.getId()));
        seatRepository.save(seat);

        // 6. Event publishen für Cache-Invalidierung (AVAILABLE → HELD)
        SeatStatusChangedEvent event = SeatStatusChangedEvent.holdCreated(
            seatId,
            seat.getConcertId(),
            userId
        );
        eventPublisher.publishEvent(event);

        logger.info("Hold created: holdId={}, seatId={}, expiresAt={}",
            reservation.getId(), seatId, reservation.getExpiresAt());

        // 7. Response DTO erstellen
        int ttlSeconds = (int) Duration.ofMinutes(holdTtlMinutes).getSeconds();
        return new HoldResponseDTO(
            String.valueOf(reservation.getId()),
            String.valueOf(seatId),
            ttlSeconds,
            reservation.getExpiresAt()
        );
    }

    /**
     * Storniert einen Hold (manuelle Freigabe).
     * 
     * Business Rules:
     * - Hold muss existieren
     * - Hold muss ACTIVE sein
     * - Seat wird auf AVAILABLE zurückgesetzt
     * 
     * @param holdId ID des Holds
     * @throws IllegalArgumentException wenn Hold nicht existiert
     * @throws IllegalStateException wenn Hold nicht ACTIVE ist
     */
    @Transactional
    public void releaseHold(Long holdId) {
        logger.info("Releasing hold: holdId={}", holdId);

        // 1. Reservation laden
        Reservation reservation = reservationRepository.findById(holdId)
            .orElseThrow(() -> new ReservationNotFoundException(holdId));

        if (!reservation.isActive()) {
            throw new IllegalStateException(
                String.format("Hold %d ist nicht aktiv (status=%s)", 
                    holdId, reservation.getStatus())
            );
        }

        // 2. Seat freigeben
        Seat seat = seatRepository.findById(reservation.getSeatId())
            .orElseThrow(() -> new IllegalStateException(
                String.format("Seat %d für Hold %d nicht gefunden", 
                    reservation.getSeatId(), holdId)
            ));

        if (seat.getStatus() == SeatStatus.HELD) {
            seat.releaseHold();
            seatRepository.save(seat);

            // Event publishen für Cache-Invalidierung (HELD → AVAILABLE)
            SeatStatusChangedEvent event = SeatStatusChangedEvent.holdCancelled(
                reservation.getSeatId(),
                seat.getConcertId(),
                reservation.getUserId()
            );
            eventPublisher.publishEvent(event);
        }

        // 3. Reservation löschen
        reservationRepository.delete(reservation);

        logger.info("Hold released: holdId={}, seatId={}", holdId, reservation.getSeatId());
    }

    /**
     * Findet einen Hold by ID.
     */
    @Transactional(readOnly = true)
    public HoldResponseDTO getHold(Long holdId) {
        Reservation reservation = reservationRepository.findById(holdId)
            .orElseThrow(() -> new ReservationNotFoundException(holdId));

        int ttlSeconds = (int) Duration.between(
            java.time.LocalDateTime.now(), 
            reservation.getExpiresAt()
        ).getSeconds();

        return new HoldResponseDTO(
            String.valueOf(reservation.getId()),
            String.valueOf(reservation.getSeatId()),
            Math.max(0, ttlSeconds),
            reservation.getExpiresAt()
        );
    }
}

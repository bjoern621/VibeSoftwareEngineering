package com.concertcomparison.infrastructure.scheduler;

import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service zum Aufräumen abgelaufener Holds.
 * Findet expired Reservations + zugehörige Seats, released beide.
 */
@Service
public class HoldCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(HoldCleanupService.class);

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ApplicationEventPublisher eventPublisher;

    public HoldCleanupService(
            ReservationRepository reservationRepository,
            SeatRepository seatRepository,
            ApplicationEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Findet und löscht alle abgelaufenen Holds.
     * 
     * Business Logic:
     * 1. Finde alle Reservations mit status=ACTIVE und expiresAt < now
     * 2. Für jede Reservation:
     *    - Lade zugehörigen Seat
     *    - Setze Seat auf AVAILABLE (releaseHold())
     *    - Lösche Reservation
     * 
     * @return Anzahl der gelöschten Holds
     */
    @Transactional
    public int cleanupExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Starting hold cleanup at {}", now);

        // 1. Finde expired Holds
        List<Reservation> expiredReservations = reservationRepository.findExpired(now);
        
        if (expiredReservations.isEmpty()) {
            logger.debug("No expired holds found");
            return 0;
        }

        logger.info("Found {} expired holds to cleanup", expiredReservations.size());

        int cleaned = 0;
        for (Reservation reservation : expiredReservations) {
            try {
                // 2. Lade Seat
                Seat seat = seatRepository.findById(reservation.getSeatId())
                    .orElse(null);

                if (seat != null && seat.getStatus() == SeatStatus.HELD) {
                    // 3. Release Seat
                    seat.releaseHold();
                    seatRepository.save(seat);
                    
                    // 4. Event publishen für Cache-Invalidierung (HELD → AVAILABLE)
                    SeatStatusChangedEvent event = SeatStatusChangedEvent.holdExpired(
                        seat.getId(), 
                        seat.getConcertId()
                    );
                    eventPublisher.publishEvent(event);
                    
                    logger.debug("Released seat {} from expired hold {}", 
                        seat.getId(), reservation.getId());
                }

                // 5. Lösche Reservation
                reservationRepository.delete(reservation);
                cleaned++;
                
                logger.info("Cleaned up expired hold: holdId={}, seatId={}, expiredAt={}", 
                    reservation.getId(), 
                    reservation.getSeatId(), 
                    reservation.getExpiresAt());
                    
            } catch (Exception e) {
                logger.error("Error cleaning up hold {}: {}", 
                    reservation.getId(), e.getMessage(), e);
                // Continue with next reservation
            }
        }

        logger.info("Hold cleanup completed: {} holds cleaned", cleaned);
        return cleaned;
    }
}

package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.SeatNotHeldException;
import com.concertcomparison.domain.model.Order;
import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service für Order-Management (Use Case: Ticket kaufen).
 * 
 * <p>Orchestriert den Kaufprozess: Validierung von Holds, Statusübergang HELD → SOLD,
 * Order-Erstellung und Hold-Bereinigung.</p>
 * 
 * <p><b>Transaktionale Garantien:</b></p>
 * <ul>
 *   <li>Atomare Updates: Seat, Reservation und Order werden gemeinsam committed</li>
 *   <li>Optimistic Locking verhindert Lost Updates bei Concurrency</li>
 *   <li>Bei Fehler wird gesamte Transaktion zurückgerollt</li>
 * </ul>
 */
@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public OrderApplicationService(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            SeatRepository seatRepository
    ) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Kauft ein Ticket basierend auf einer aktiven Reservation (Hold).
     * 
     * <p><b>Ablauf:</b></p>
     * <ol>
     *   <li>Validierung: Reservation existiert und ist aktiv</li>
     *   <li>Validierung: Reservation ist nicht abgelaufen</li>
     *   <li>Validierung: Reservation gehört zum angegebenen User</li>
     *   <li>Validierung: Seat ist HELD mit korrekter Reservation-ID</li>
     *   <li>Statusübergang: Seat HELD → SOLD (via seat.sell())</li>
     *   <li>Statusübergang: Reservation ACTIVE → PURCHASED</li>
     *   <li>Order-Erstellung und Speicherung</li>
     * </ol>
     * 
     * @param reservationId ID der zu kaufenden Reservation
     * @param userId        ID des Käufers (muss Owner der Reservation sein)
     * @return OrderResponseDTO mit Order-Details
     * @throws IllegalArgumentException      wenn Reservation nicht existiert
     * @throws IllegalStateException         wenn Hold abgelaufen ist
     * @throws IllegalArgumentException      wenn userId nicht übereinstimmt
     * @throws SeatNotHeldException          wenn Seat nicht HELD ist
     * @throws OptimisticLockingFailureException bei Concurrency-Konflikt
     */
    @Transactional
    public OrderResponseDTO purchaseTicket(Long reservationId, String userId) {
        log.info("Starte Ticket-Kauf für Reservation {} von User {}", reservationId, userId);

        // 1. Reservation laden und validieren
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.warn("Reservation {} nicht gefunden", reservationId);
                    return new IllegalArgumentException("Reservation nicht gefunden");
                });

        // 2. Prüfen: Reservation ist nicht abgelaufen (BEFORE active check for specific error)
        if (reservation.isExpired()) {
            log.warn("Reservation {} ist abgelaufen (ExpiresAt: {})", reservationId, reservation.getExpiresAt());
            throw new IllegalStateException("Reservation ist abgelaufen");
        }

        // 3. Prüfen: Reservation ist aktiv
        if (!reservation.isActive()) {
            log.warn("Reservation {} ist nicht aktiv (Status: {})", reservationId, reservation.getStatus());
            throw new IllegalStateException("Reservation ist nicht aktiv");
        }

        // 4. Prüfen: User ist Owner der Reservation
        if (!reservation.getUserId().equals(userId)) {
            log.warn("User {} versucht fremde Reservation {} zu kaufen (Owner: {})",
                    userId, reservationId, reservation.getUserId());
            throw new IllegalArgumentException("Reservation gehört nicht dem angegebenen User");
        }

        // 5. Seat laden
        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> {
                    log.error("Seat {} für Reservation {} nicht gefunden", reservation.getSeatId(), reservationId);
                    return new IllegalStateException("Seat nicht gefunden");
                });

        // 6. Seat verkaufen (HELD → SOLD) - wirft SeatNotHeldException wenn nicht HELD
        String reservationIdString = String.valueOf(reservationId);
        seat.sell(reservationIdString);
        seatRepository.save(seat);
        log.info("Seat {} erfolgreich verkauft (HELD → SOLD)", seat.getId());

        // 7. Reservation als gekauft markieren (ACTIVE → PURCHASED)
        reservation.markAsPurchased();
        reservationRepository.save(reservation);
        log.info("Reservation {} als gekauft markiert (ACTIVE → PURCHASED)", reservationId);

        // 8. Order erstellen
        Order order = Order.createFromReservation(reservation, seat);
        order = orderRepository.save(order);
        log.info("Order {} erfolgreich erstellt für User {} (Seat {}, Preis: {}€)",
                order.getId(), userId, seat.getId(), order.getTotalPrice());

        // 9. DTO mapping und Rückgabe
        return mapToOrderResponseDTO(order, seat);
    }

    /**
     * Holt Order-Details anhand der Order-ID.
     * 
     * @param orderId ID der Order
     * @return OrderResponseDTO mit Order-Details
     * @throws IllegalArgumentException wenn Order nicht existiert
     */
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        log.debug("Lade Order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order nicht gefunden"));

        // Seat laden für zusätzliche Informationen
        Seat seat = seatRepository.findById(order.getSeatId())
                .orElseThrow(() -> new IllegalStateException("Seat nicht gefunden"));

        return mapToOrderResponseDTO(order, seat);
    }

    /**
     * Holt alle Orders eines Users.
     * 
     * @param userId User-ID
     * @return Liste von OrderResponseDTOs (leer, wenn keine vorhanden)
     */
    @Transactional(readOnly = true)
    public java.util.List<OrderResponseDTO> getOrdersByUserId(String userId) {
        log.debug("Lade alle Orders für User {}", userId);

        return orderRepository.findByUserId(userId).stream()
                .map(order -> {
                    Seat seat = seatRepository.findById(order.getSeatId())
                            .orElseThrow(() -> new IllegalStateException("Seat nicht gefunden"));
                    return mapToOrderResponseDTO(order, seat);
                })
                .toList();
    }

    // ========== DTO Mapping ==========

    private OrderResponseDTO mapToOrderResponseDTO(Order order, Seat seat) {
        return OrderResponseDTO.builder()
                .orderId(String.valueOf(order.getId()))
                .seatId(String.valueOf(order.getSeatId()))
                .userId(order.getUserId())
                .seatNumber(seat.getSeatNumber())
                .category(seat.getCategory())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}

package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.ReservationExpiredException;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Application Service für Order-Operationen (US-03).
 * 
 * Orchestriert Domain Entities (Order, Seat, Reservation) und Repositories.
 * Implementiert den Purchase-Flow: Hold → Order (HELD → SOLD).
 */
@Service
public class OrderApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public OrderApplicationService(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            SeatRepository seatRepository) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Kauft ein Ticket basierend auf einer aktiven Reservation.
     * 
     * Business Rules:
     * - Hold muss existieren
     * - Hold darf nicht abgelaufen sein (expiresAt > now)
     * - Hold muss zum User gehören
     * - Seat muss HELD sein
     * - Transaktion: Hold löschen + Seat auf SOLD + Order erstellen
     * 
     * @param holdId ID der Reservation
     * @param userId ID des Käufers
     * @return Erstellte Order
     * @throws IllegalArgumentException wenn Hold nicht existiert
     * @throws ReservationExpiredException wenn Hold abgelaufen ist
     * @throws IllegalStateException wenn Hold nicht zum User gehört
     * @throws OptimisticLockException bei Concurrency Conflict
     */
    @Transactional
    public Order purchaseTicket(Long holdId, String userId) {
        logger.info("Starting purchase: holdId={}, userId={}", holdId, userId);

        // 1. Reservation laden und validieren
        Reservation reservation = reservationRepository.findById(holdId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Reservation mit ID %d nicht gefunden", holdId)
            ));

        // 2. Prüfen ob Reservation abgelaufen ist
        if (reservation.isExpired()) {
            logger.warn("Purchase failed: Reservation {} is expired (expiresAt={})", 
                holdId, reservation.getExpiresAt());
            throw new ReservationExpiredException(
                String.format("Reservation %d ist abgelaufen (Ablauf: %s)", 
                    holdId, reservation.getExpiresAt())
            );
        }

        // 3. Prüfen ob Reservation zum User gehört
        if (!reservation.getUserId().equals(userId)) {
            logger.warn("Purchase failed: Reservation {} belongs to different user", holdId);
            throw new IllegalStateException(
                String.format("Reservation %d gehört nicht zum User %s", holdId, userId)
            );
        }

        // 4. Seat laden
        Seat seat = seatRepository.findById(reservation.getSeatId())
            .orElseThrow(() -> new IllegalStateException(
                String.format("Seat mit ID %d nicht gefunden", reservation.getSeatId())
            ));

        // 5. Prüfen ob Seat HELD ist
        if (seat.getStatus() != SeatStatus.HELD) {
            logger.warn("Purchase failed: Seat {} is not HELD (status={})", 
                seat.getId(), seat.getStatus());
            throw new IllegalStateException(
                String.format("Seat %d ist nicht reserviert (Status: %s)", 
                    seat.getId(), seat.getStatus())
            );
        }

        // 6. Seat auf SOLD setzen (Domain Logic)
        seat.sell(String.valueOf(holdId));
        seatRepository.save(seat);

        // 7. Order erstellen (Domain Logic)
        Order order = Order.createOrder(
            seat.getId(),
            userId,
            seat.getPrice(),
            PaymentMethod.CREDIT_CARD  // Default für MVP
        );

        // 8. Payment completen und Order bestätigen (via Aggregate Root)
        // DDD: Payment.complete() ist package-private, nur Order darf es aufrufen
        order.completePayment("TXN-" + System.currentTimeMillis());

        // 9. Order speichern
        order = orderRepository.save(order);

        // 9. Reservation löschen (Hold wird nicht mehr benötigt)
        reservationRepository.delete(reservation);

        logger.info("Purchase completed: orderId={}, seatId={}, userId={}, price={}", 
            order.getId(), seat.getId(), userId, seat.getPrice());

        return order;
    }

    /**
     * Sucht eine Order anhand der ID.
     * 
     * @param orderId Order-ID
     * @return Order
     * @throws IllegalArgumentException wenn Order nicht existiert
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Order mit ID %d nicht gefunden", orderId)
            ));
    }

    /**
     * Sucht alle Orders eines Users.
     * 
     * @param userId User-ID
     * @return Liste der Orders, sortiert nach Kaufdatum absteigend
     */
    @Transactional(readOnly = true)
    public java.util.List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
}

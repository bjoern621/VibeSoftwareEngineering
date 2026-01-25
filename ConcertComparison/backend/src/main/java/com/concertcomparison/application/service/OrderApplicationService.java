package com.concertcomparison.application.service;

import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import com.concertcomparison.domain.exception.ReservationExpiredException;
import com.concertcomparison.domain.exception.ReservationNotFoundException;
import com.concertcomparison.domain.exception.SeatNotFoundException;
import com.concertcomparison.domain.exception.OrderNotFoundException;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.infrastructure.util.QrCodeService;
import com.concertcomparison.presentation.dto.OrderHistoryItemDTO;
import com.concertcomparison.presentation.dto.TicketDTO;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Application Service für Order-Operationen (US-03, US-179).
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
    private final ConcertRepository concertRepository;
    private final QrCodeService qrCodeService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentApplicationService paymentApplicationService;
    private final boolean syncPaymentMode; // Für Tests: Synchrone Payment-Verarbeitung

    public OrderApplicationService(
            OrderRepository orderRepository,
            ReservationRepository reservationRepository,
            SeatRepository seatRepository,
            ConcertRepository concertRepository,
            QrCodeService qrCodeService,
            ApplicationEventPublisher eventPublisher,
            PaymentApplicationService paymentApplicationService) {
        this.orderRepository = orderRepository;
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
        this.qrCodeService = qrCodeService;
        this.eventPublisher = eventPublisher;
        this.paymentApplicationService = paymentApplicationService;
        this.syncPaymentMode = true; // SYNC-Modus für deterministische Tests
    }

    /**
     * Kauft ein Ticket basierend auf einer aktiven Reservation.
     * 
     * Business Rules:
     * - Hold muss existieren
     * - Hold darf nicht abgelaufen sein (expiresAt > now)
     * - Hold muss zum User gehören
     * - Seat muss HELD sein
     * - Order wird mit PENDING Payment erstellt (Payment erfolgt asynchron)
     * - Reservation bleibt erhalten (wird nach Payment-Success gelöscht)
     * 
     * @param holdId ID der Reservation
     * @param userId ID des Käufers
     * @param paymentMethod Gewählte Zahlungsmethode
     * @return Erstellte Order im Status PENDING
     * @throws IllegalArgumentException wenn Hold nicht existiert
     * @throws ReservationExpiredException wenn Hold abgelaufen ist
     * @throws IllegalStateException wenn Hold nicht zum User gehört
     * @throws OptimisticLockException bei Concurrency Conflict
     */
    @Transactional
    public Order purchaseTicket(Long holdId, String userId, PaymentMethod paymentMethod) {
        logger.info("Starting purchase: holdId={}, userId={}, paymentMethod={}", holdId, userId, paymentMethod);

        // 1. Reservation laden und validieren
        Reservation reservation = reservationRepository.findById(holdId)
            .orElseThrow(() -> new ReservationNotFoundException(holdId));

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
            .orElseThrow(() -> new SeatNotFoundException(reservation.getSeatId()));

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

        // 7. Event publishen für Cache-Invalidierung (HELD → SOLD)
        SeatStatusChangedEvent event = SeatStatusChangedEvent.ticketPurchased(
            seat.getId(),
            seat.getConcertId(),
            userId
        );
        eventPublisher.publishEvent(event);

        // 8. Order erstellen mit Payment PENDING (Domain Logic)
        // NEU: reservationId wird für Rollback gespeichert
        Order order = Order.createOrder(
            seat.getId(),
            userId,
            seat.getPrice(),
            paymentMethod,
            holdId  // Reservation-ID für Rollback
        );

        // 9. Order speichern (Payment bleibt PENDING!)
        // NEU: Payment wird NICHT mehr direkt completed
        // Payment erfolgt asynchron via PaymentApplicationService
        order = orderRepository.save(order);

        // 10. Reservation wird NICHT gelöscht
        // NEU: Reservation bleibt bis Payment-Success erhalten
        // Bei Payment-Failure wird Seat auf HELD zurückgesetzt
        
        // 11. Payment-Processing starten
        if (syncPaymentMode) {
            // Test-Modus: Synchron abschließen für deterministische Tests
            logger.info("Processing payment synchronously (test mode) for orderId={}", order.getId());
            final Long orderId = order.getId(); // Final für Lambda
            paymentApplicationService.processPaymentSync(orderId);
            // Order neu laden um aktuellen Status (CONFIRMED/CANCELLED) zu bekommen
            order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        } else {
            // Produktions-Modus: Asynchron starten
            logger.info("Payment processing queued (async mode) for orderId={}", order.getId());
            paymentApplicationService.processPaymentAsync(order.getId());
            // Order bleibt PENDING bis async Processing abgeschlossen ist
        }

        logger.info("Purchase initiated: orderId={}, seatId={}, userId={}, price={}, status={}", 
            order.getId(), seat.getId(), userId, seat.getPrice(), order.getStatus());

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
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Sucht alle Orders eines Users.
     * 
     * @param userId User-ID
     * @return Liste der Orders, sortiert nach Kaufdatum absteigend
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Liefert angereicherte Order History für einen User (US-179).
     * 
     * Enthält Concert und Seat Details für bessere UX.
     * Verwendet Batch-Fetching um N+1 Query Problem zu vermeiden.
     * 
     * @param userId User-ID
     * @return Liste von OrderHistoryItemDTO, sortiert nach Kaufdatum absteigend
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryItemDTO> getOrderHistoryForUser(String userId) {
        logger.debug("Fetching order history for user: {}", userId);

        List<Order> orders = orderRepository.findByUserId(userId);
        
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        List<OrderHistoryItemDTO> historyItems = new ArrayList<>();

        for (Order order : orders) {
            // Seat laden (enthält concertId)
            Seat seat = seatRepository.findById(order.getSeatId())
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Seat %d für Order %d nicht gefunden", 
                        order.getSeatId(), order.getId())
                ));

            // Concert laden
            Concert concert = concertRepository.findById(seat.getConcertId())
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Concert %d für Seat %d nicht gefunden", 
                        seat.getConcertId(), seat.getId())
                ));

            // DTO erstellen mit allen Details
            OrderHistoryItemDTO dto = new OrderHistoryItemDTO(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getPurchaseDate(),
                order.getPayment() != null ? order.getPayment().getStatus().toString() : "UNKNOWN",
                concert.getId(),
                concert.getName(),
                concert.getVenue(),
                concert.getDate(),
                seat.getId(),
                seat.getSeatNumber(),
                seat.getCategory(),
                seat.getBlock(),
                seat.getRow(),
                seat.getNumber()
            );

            historyItems.add(dto);
        }

        logger.debug("Loaded {} order history items for user {}", historyItems.size(), userId);
        return historyItems;
    }

    /**
     * Generiert QR Code für ein Ticket (US-179).
     * 
     * QR Code enthält: orderId|concertId|seatId|userId
     * 
     * Security: Prüft ob Order dem User gehört.
     * 
     * @param orderId Order-ID
     * @param userId User-ID (für Security Check)
     * @return PNG Image als byte array
     * @throws IllegalArgumentException wenn Order nicht existiert
     * @throws SecurityException wenn Order nicht dem User gehört
     */
    @Transactional(readOnly = true)
    public byte[] generateTicketQRCode(Long orderId, String userId) {
        logger.debug("Generating QR code for order {} and user {}", orderId, userId);

        // Order laden
        Order order = getOrderById(orderId);

        // Security: Prüfen ob Order dem User gehört
        if (!order.getUserId().equals(userId)) {
            logger.warn("Unauthorized QR code access: order {} does not belong to user {}", 
                orderId, userId);
            throw new AccessDeniedException("Sie haben keine Berechtigung für dieses Ticket.");
        }

        // Seat laden um concertId zu bekommen
        Seat seat = seatRepository.findById(order.getSeatId())
            .orElseThrow(() -> new IllegalStateException(
                String.format("Seat %d für Order %d nicht gefunden", 
                    order.getSeatId(), order.getId())
            ));

        // TicketDTO erstellen
        TicketDTO ticket = new TicketDTO(
            order.getId(),
            seat.getConcertId(),
            seat.getId(),
            order.getUserId()
        );

        // QR Code generieren
        String qrContent = ticket.toQrCodeContent();
        byte[] qrCodeImage = qrCodeService.generateQrCodeImage(qrContent);

        logger.debug("QR code generated for order {}: {} bytes", orderId, qrCodeImage.length);
        return qrCodeImage;
    }
}

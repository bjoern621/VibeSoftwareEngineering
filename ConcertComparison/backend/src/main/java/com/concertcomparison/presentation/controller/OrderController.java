package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.OrderApplicationService;
import com.concertcomparison.domain.exception.ReservationExpiredException;
import com.concertcomparison.domain.model.Order;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import com.concertcomparison.presentation.dto.PurchaseTicketRequestDTO;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller für Order-Operationen (US-03).
 * 
 * Endpoints:
 * - POST /api/orders - Ticket kaufen
 * - GET /api/orders/{id} - Order-Details abrufen
 * - GET /api/users/{userId}/orders - Alle Orders eines Users
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    /**
     * Kauft ein Ticket basierend auf einer aktiven Reservation.
     * 
     * Endpoint: POST /api/orders
     * Request Body: { "holdId": 123, "userId": "user@example.com" }
     * 
     * Success Response: 201 Created
     * {
     *   "orderId": 1,
     *   "seatId": 42,
     *   "userId": "user@example.com",
     *   "totalPrice": 99.99,
     *   "status": "CONFIRMED",
     *   "purchaseDate": "2026-01-20T15:30:00",
     *   "paymentStatus": "COMPLETED"
     * }
     * 
     * Error Responses:
     * - 400 Bad Request: Invalid request data
     * - 404 Not Found: Reservation nicht gefunden
     * - 409 Conflict: Reservation abgelaufen oder Optimistic Lock Fehler
     * - 500 Internal Server Error: Unerwarteter Fehler
     * 
     * @param request Purchase Request (holdId, userId)
     * @return OrderResponseDTO
     */
    @PostMapping
    public ResponseEntity<?> purchaseTicket(@Valid @RequestBody PurchaseTicketRequestDTO request) {
        logger.info("POST /api/orders - holdId={}, userId={}", request.getHoldId(), request.getUserId());

        try {
            // 1. Ticket kaufen via Application Service
            Order order = orderApplicationService.purchaseTicket(
                request.getHoldId(),
                request.getUserId()
            );

            // 2. Order → DTO mappen
            OrderResponseDTO response = mapToDTO(order);

            logger.info("Purchase successful: orderId={}, seatId={}", order.getId(), order.getSeatId());

            // 3. 201 Created zurückgeben
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

        } catch (IllegalArgumentException e) {
            // Reservation nicht gefunden oder ungültige Parameter
            logger.warn("Purchase failed (Not Found): {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));

        } catch (ReservationExpiredException e) {
            // Reservation abgelaufen
            logger.warn("Purchase failed (Expired): {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));

        } catch (IllegalStateException e) {
            // Seat nicht HELD oder Reservation gehört anderem User
            logger.warn("Purchase failed (Conflict): {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));

        } catch (OptimisticLockException e) {
            // Concurrency Conflict
            logger.warn("Purchase failed (Optimistic Lock): {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Gleichzeitiger Zugriff erkannt. Bitte erneut versuchen."));

        } catch (Exception e) {
            // Unerwarteter Fehler
            logger.error("Purchase failed (Unexpected Error): {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Ein unerwarteter Fehler ist aufgetreten."));
        }
    }

    /**
     * Ruft Order-Details ab.
     * 
     * Endpoint: GET /api/orders/{id}
     * 
     * Success Response: 200 OK
     * Error Response: 404 Not Found
     * 
     * @param orderId Order-ID
     * @return OrderResponseDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") Long orderId) {
        logger.info("GET /api/orders/{}", orderId);

        try {
            Order order = orderApplicationService.getOrderById(orderId);
            OrderResponseDTO response = mapToDTO(order);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Order not found: {}", orderId);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Ruft alle Orders eines Users ab.
     * 
     * Endpoint: GET /api/users/{userId}/orders
     * 
     * Success Response: 200 OK (Liste von Orders)
     * 
     * @param userId User-ID
     * @return Liste von OrderResponseDTO
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable String userId) {
        logger.info("GET /api/users/{}/orders", userId);

        List<Order> orders = orderApplicationService.getOrdersByUserId(userId);
        List<OrderResponseDTO> response = orders.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Mappt Order Entity → OrderResponseDTO.
     */
    private OrderResponseDTO mapToDTO(Order order) {
        return new OrderResponseDTO(
            order.getId(),
            order.getSeatId(),
            order.getUserId(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getPurchaseDate(),
            order.getPayment() != null ? order.getPayment().getStatus().toString() : "UNKNOWN"
        );
    }

    /**
     * Error Response DTO für konsistente Fehlerausgabe.
     */
    private static class ErrorResponse {
        private final String error;
        private final long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() {
            return error;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}

package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.OrderApplicationService;
import com.concertcomparison.domain.exception.ReservationExpiredException;
import com.concertcomparison.domain.model.Order;
import com.concertcomparison.presentation.dto.OrderHistoryItemDTO;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import com.concertcomparison.presentation.dto.PurchaseTicketRequestDTO;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller für Order-Operationen (US-03, US-179).
 * 
 * Endpoints:
 * - POST /api/orders - Ticket kaufen
 * - GET /api/orders/{id} - Order-Details abrufen (mit Ownership Check)
 * - GET /api/users/me/orders - Orders des aktuellen Users (US-179)
 * - GET /api/orders/{id}/ticket - QR Code für Ticket (US-179)
 */
@RestController
@RequestMapping("/api")
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
    @PostMapping("/orders")
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
    @GetMapping("/orders/{id}")
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
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable String userId) {
        logger.info("GET /api/users/{}/orders", userId);

        List<Order> orders = orderApplicationService.getOrdersByUserId(userId);
        List<OrderResponseDTO> response = orders.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Ruft Order History des aktuellen Users ab (US-179).
     * 
     * Endpoint: GET /api/users/me/orders
     * 
     * Enthält angereicherte Informationen (Concert, Seat Details).
     * Security: Nur authenticated User kann seine eigenen Orders abrufen.
     * 
     * Success Response: 200 OK
     * [
     *   {
     *     "orderId": 1,
     *     "status": "CONFIRMED",
     *     "totalPrice": 99.99,
     *     "purchaseDate": "2026-01-20T15:30:00",
     *     "paymentStatus": "COMPLETED",
     *     "concertId": 1,
     *     "concertName": "Rock Festival 2026",
     *     "venue": "Olympiastadion Berlin",
     *     "concertDate": "2026-07-15T20:00:00",
     *     "seatId": 42,
     *     "seatNumber": "A-1-5",
     *     "category": "VIP",
     *     "block": "A",
     *     "row": "1",
     *     "number": "5"
     *   }
     * ]
     * 
     * @param authentication Spring Security Authentication (JWT)
     * @return Liste von OrderHistoryItemDTO
     */
    @GetMapping("/users/me/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderHistoryItemDTO>> getCurrentUserOrderHistory(
            Authentication authentication) {
        
        String userId = authentication.getName();
        logger.info("GET /api/users/me/orders - userId={}", userId);

        List<OrderHistoryItemDTO> orderHistory = 
            orderApplicationService.getOrderHistoryForUser(userId);

        return ResponseEntity.ok(orderHistory);
    }

    /**
     * Generiert QR Code für ein Ticket (US-179).
     * 
     * Endpoint: GET /api/orders/{id}/ticket
     * 
     * Security: User darf nur QR Codes seiner eigenen Orders abrufen.
     * 
     * QR Code Content: orderId|concertId|seatId|userId
     * Format: PNG Image (300x300px)
     * 
     * Success Response: 200 OK (image/png)
     * Error Responses:
     * - 403 Forbidden: Order gehört anderem User
     * - 404 Not Found: Order nicht gefunden
     * 
     * @param orderId Order-ID
     * @param authentication Spring Security Authentication (JWT)
     * @return PNG Image als byte array
     */
    @GetMapping("/orders/{id}/ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTicketQRCode(
            @PathVariable("id") Long orderId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        logger.info("GET /api/orders/{}/ticket - userId={}", orderId, userId);

        try {
            byte[] qrCode = orderApplicationService.generateTicketQRCode(orderId, userId);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("Content-Disposition", 
                    "attachment; filename=\"ticket-" + orderId + ".png\"")
                .body(qrCode);

        } catch (IllegalArgumentException e) {
            // Order nicht gefunden
            logger.warn("Ticket QR code failed (Not Found): {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));

        } catch (SecurityException e) {
            // Order gehört anderem User
            logger.warn("Ticket QR code failed (Forbidden): {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Sie haben keine Berechtigung für dieses Ticket."));

        } catch (Exception e) {
            // Unerwarteter Fehler
            logger.error("Ticket QR code failed (Unexpected Error): {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Fehler beim Generieren des QR Codes."));
        }
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

package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.OrderApplicationService;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import com.concertcomparison.presentation.dto.PurchaseRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller für Order-Management (Ticket-Kauf).
 * 
 * <p>Bietet Endpoints für:</p>
 * <ul>
 *   <li>POST /api/orders - Ticket kaufen</li>
 *   <li>GET /api/orders/{id} - Order-Details abrufen</li>
 *   <li>GET /api/users/me/orders - Eigene Orders abrufen</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Ticket-Kauf und Order-Verwaltung")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    /**
     * Kauft ein Ticket basierend auf einer aktiven Reservation.
     * 
     * <p><b>US-03: Ticket kaufen</b></p>
     * <ul>
     *   <li>Validiert aktive, nicht-abgelaufene Hold</li>
     *   <li>Führt transaktionalen Statusübergang durch (HELD → SOLD)</li>
     *   <li>Erstellt Order und markiert Reservation als PURCHASED</li>
     * </ul>
     * 
     * @param request Purchase-Request mit reservationId und userId
     * @return OrderResponseDTO mit Order-Details
     */
    @PostMapping
    @Operation(summary = "Ticket kaufen", description = "Kauft ein Ticket basierend auf einer aktiven Reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kauf erfolgreich"),
            @ApiResponse(responseCode = "400", description = "Ungültige Request-Daten"),
            @ApiResponse(responseCode = "404", description = "Reservation nicht gefunden"),
            @ApiResponse(responseCode = "409", description = "Reservation abgelaufen oder nicht aktiv")
    })
    public ResponseEntity<OrderResponseDTO> purchaseTicket(@Valid @RequestBody PurchaseRequestDTO request) {
        log.info("POST /api/orders - Purchase-Request für Reservation {} von User {}",
                request.getReservationId(), request.getUserId());

        Long reservationId = Long.parseLong(request.getReservationId());
        OrderResponseDTO order = orderApplicationService.purchaseTicket(reservationId, request.getUserId());

        log.info("Ticket-Kauf erfolgreich - Order {} erstellt", order.getOrderId());
        return ResponseEntity.ok(order);
    }

    /**
     * Ruft Details einer Order ab.
     * 
     * @param id Order-ID
     * @return OrderResponseDTO mit Order-Details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Order-Details abrufen", description = "Ruft Details einer Order anhand ihrer ID ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order gefunden"),
            @ApiResponse(responseCode = "404", description = "Order nicht gefunden")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable String id) {
        log.info("GET /api/orders/{}", id);

        Long orderId = Long.parseLong(id);
        OrderResponseDTO order = orderApplicationService.getOrderById(orderId);

        return ResponseEntity.ok(order);
    }

    /**
     * Ruft alle Orders des aktuellen Users ab.
     * 
     * @param userId User-ID (aus Security-Context oder Request-Parameter)
     * @return Liste von OrderResponseDTOs
     */
    @GetMapping
    @Operation(summary = "Eigene Orders abrufen", description = "Ruft alle Orders des aktuellen Users ab")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders erfolgreich abgerufen")
    })
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(@RequestParam String userId) {
        log.info("GET /api/orders?userId={}", userId);

        List<OrderResponseDTO> orders = orderApplicationService.getOrdersByUserId(userId);

        log.info("Gefundene Orders für User {}: {}", userId, orders.size());
        return ResponseEntity.ok(orders);
    }
}

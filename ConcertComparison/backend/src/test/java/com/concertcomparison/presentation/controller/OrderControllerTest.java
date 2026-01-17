package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.OrderApplicationService;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import com.concertcomparison.presentation.dto.PurchaseRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für OrderController.
 * 
 * <p>Testet REST API Endpoints mit MockMvc.</p>
 */
@WebMvcTest(OrderController.class)
@DisplayName("OrderController Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderApplicationService orderApplicationService;

    private static final String ORDER_ID = "999";
    private static final String RESERVATION_ID = "1";
    private static final String SEAT_ID = "100";
    private static final String USER_ID = "user123";
    private static final Double PRICE = 99.99;

    // ========== POST /api/orders Tests ==========

    @Test
    @WithMockUser
    @DisplayName("POST /api/orders sollte Order erstellen bei gültigem Request")
    void purchaseTicket_ShouldReturnOrder_WhenRequestIsValid() throws Exception {
        // Arrange
        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .reservationId(RESERVATION_ID)
                .userId(USER_ID)
                .build();

        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(ORDER_ID)
                .seatId(SEAT_ID)
                .userId(USER_ID)
                .seatNumber("A-1")
                .category("VIP")
                .totalPrice(PRICE)
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .build();

        given(orderApplicationService.purchaseTicket(1L, USER_ID)).willReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.seatId").value(SEAT_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.seatNumber").value("A-1"))
                .andExpect(jsonPath("$.category").value("VIP"))
                .andExpect(jsonPath("$.totalPrice").value(PRICE))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/orders sollte 400 zurückgeben bei fehlendem reservationId")
    void purchaseTicket_ShouldReturn400_WhenReservationIdMissing() throws Exception {
        // Arrange
        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .userId(USER_ID)
                // reservationId fehlt
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/orders sollte 400 zurückgeben bei fehlendem userId")
    void purchaseTicket_ShouldReturn400_WhenUserIdMissing() throws Exception {
        // Arrange
        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .reservationId(RESERVATION_ID)
                // userId fehlt
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/orders sollte 404 zurückgeben wenn Reservation nicht existiert")
    void purchaseTicket_ShouldReturn404_WhenReservationNotFound() throws Exception {
        // Arrange
        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .reservationId(RESERVATION_ID)
                .userId(USER_ID)
                .build();

        given(orderApplicationService.purchaseTicket(any(), any()))
                .willThrow(new IllegalArgumentException("Reservation nicht gefunden"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Reservation nicht gefunden"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/orders sollte 409 zurückgeben wenn Reservation abgelaufen ist")
    void purchaseTicket_ShouldReturn409_WhenReservationExpired() throws Exception {
        // Arrange
        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .reservationId(RESERVATION_ID)
                .userId(USER_ID)
                .build();

        given(orderApplicationService.purchaseTicket(any(), any()))
                .willThrow(new IllegalStateException("Reservation ist abgelaufen"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("Reservation ist abgelaufen"));
    }

    // ========== GET /api/orders/{id} Tests ==========

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders/{id} sollte Order zurückgeben")
    void getOrderById_ShouldReturnOrder_WhenOrderExists() throws Exception {
        // Arrange
        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(ORDER_ID)
                .seatId(SEAT_ID)
                .userId(USER_ID)
                .seatNumber("A-1")
                .category("VIP")
                .totalPrice(PRICE)
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .build();

        given(orderApplicationService.getOrderById(999L)).willReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(ORDER_ID))
                .andExpect(jsonPath("$.seatId").value(SEAT_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders/{id} sollte 404 zurückgeben wenn Order nicht existiert")
    void getOrderById_ShouldReturn404_WhenOrderNotFound() throws Exception {
        // Arrange
        given(orderApplicationService.getOrderById(any()))
                .willThrow(new IllegalArgumentException("Order nicht gefunden"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/{id}", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Order nicht gefunden"));
    }

    // ========== GET /api/orders?userId=X Tests ==========

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders?userId=X sollte Liste von Orders zurückgeben")
    void getMyOrders_ShouldReturnOrderList() throws Exception {
        // Arrange
        OrderResponseDTO order1 = OrderResponseDTO.builder()
                .orderId("1")
                .seatId("100")
                .userId(USER_ID)
                .seatNumber("A-1")
                .category("VIP")
                .totalPrice(99.99)
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .build();

        OrderResponseDTO order2 = OrderResponseDTO.builder()
                .orderId("2")
                .seatId("101")
                .userId(USER_ID)
                .seatNumber("A-2")
                .category("VIP")
                .totalPrice(89.99)
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .build();

        given(orderApplicationService.getOrdersByUserId(USER_ID)).willReturn(List.of(order1, order2));

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .param("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value("1"))
                .andExpect(jsonPath("$[1].orderId").value("2"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/orders?userId=X sollte leere Liste zurückgeben wenn keine Orders vorhanden")
    void getMyOrders_ShouldReturnEmptyList_WhenNoOrders() throws Exception {
        // Arrange
        given(orderApplicationService.getOrdersByUserId(USER_ID)).willReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                        .param("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

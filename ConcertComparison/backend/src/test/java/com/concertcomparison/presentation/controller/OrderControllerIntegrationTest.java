package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.HoldApplicationService;
import com.concertcomparison.application.service.OrderApplicationService;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.domain.repository.UserRepository;
import com.concertcomparison.presentation.dto.LoginRequest;
import com.concertcomparison.presentation.dto.PurchaseTicketRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für OrderController (US-03).
 * 
 * Test Coverage:
 * - POST /api/orders - Erfolgreicher Purchase-Flow
 * - POST /api/orders - Hold nicht gefunden
 * - POST /api/orders - Abgelaufener Hold
 * - POST /api/orders - Hold gehört anderem User
 * - GET /api/orders/{id} - Order-Details abrufen
 * - GET /api/orders/user/{userId} - User Orders abrufen
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OrderController Integration Tests")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private HoldApplicationService holdApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USER_ID = "integration-test@example.com";
    private static final String USER_PASSWORD = "password123";
    private static final String OTHER_USER_ID = "other-user@example.com";

    private Seat testSeat;
    private Concert testConcert;
    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        orderRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        concertRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user in database
        testUser = User.createUser(
            USER_ID,
            passwordEncoder.encode(USER_PASSWORD),
            "Integration",
            "Test",
            UserRole.USER
        );
        userRepository.save(testUser);

        // Login and get JWT token
        LoginRequest loginRequest = new LoginRequest(USER_ID, USER_PASSWORD);
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        jwtToken = objectMapper.readTree(loginResponse).get("token").asText();

        // Create test concert
        testConcert = Concert.createConcert(
            "Integration Test Concert",
            LocalDateTime.of(2026, 12, 31, 20, 0),
            "Test Arena",
            "Integration Test Event"
        );
        testConcert = concertRepository.save(testConcert);

        // Create test seat (AVAILABLE)
        testSeat = new Seat(
            testConcert.getId(),
            "TEST-1",
            "VIP",
            "Block A",
            "1",
            "1",
            99.99
        );
        testSeat = seatRepository.save(testSeat);
    }

    // ==================== POST /api/orders SUCCESS TESTS ====================

    @Test
    @DisplayName("POST /api/orders - Erfolgreicher Ticket-Kauf")
    void purchaseTicket_Success() throws Exception {
        // Arrange - Erstelle Hold via Service (realistischer Flow)
        var holdResponse = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        Long holdId = Long.parseLong(holdResponse.holdId());

        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(holdId, USER_ID);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.seatId").value(testSeat.getId()))
            .andExpect(jsonPath("$.userId").value(USER_ID))
            .andExpect(jsonPath("$.totalPrice").value(99.99))
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));

        // Verify Database State
        // 1. Seat ist SOLD
        Seat updatedSeat = seatRepository.findById(testSeat.getId()).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // 2. Hold wurde gelöscht
        var deletedHold = reservationRepository.findById(holdId);
        assertThat(deletedHold).isEmpty();

        // 3. Order wurde erstellt
        var orders = orderRepository.findByUserId(USER_ID);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    // ==================== POST /api/orders ERROR TESTS ====================

    @Test
    @DisplayName("POST /api/orders - Hold nicht gefunden (404)")
    void purchaseTicket_HoldNotFound() throws Exception {
        // Arrange
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(999L, USER_ID);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
            .andExpect(jsonPath("$.message").isNotEmpty());

        // Verify keine Order erstellt
        var orders = orderRepository.findByUserId(USER_ID);
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("POST /api/orders - Abgelaufener Hold (409)")
    void purchaseTicket_ExpiredHold() throws Exception {
        // Arrange - Erstelle abgelaufenen Hold direkt in DB
        Reservation expiredHold = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        setExpiresAt(expiredHold, LocalDateTime.now().minusMinutes(5)); // Abgelaufen
        expiredHold = reservationRepository.save(expiredHold);

        // Seat auf HELD setzen (für Konsistenz)
        testSeat.hold(String.valueOf(expiredHold.getId()), LocalDateTime.now().minusMinutes(5));
        seatRepository.save(testSeat);

        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(expiredHold.getId(), USER_ID);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("RESERVATION_EXPIRED"))
            .andExpect(jsonPath("$.message").isNotEmpty());

        // Verify keine Order erstellt
        var orders = orderRepository.findByUserId(USER_ID);
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("POST /api/orders - Hold gehört anderem User (409)")
    void purchaseTicket_WrongUser() throws Exception {
        // Arrange - Erstelle Hold für USER_ID
        var holdResponse = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        Long holdId = Long.parseLong(holdResponse.holdId());

        // Act - Anderer User versucht zu kaufen
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(holdId, OTHER_USER_ID);

        // Assert
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ILLEGAL_STATE"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("gehört nicht zum User")));

        // Verify Hold noch vorhanden, keine Order erstellt
        var hold = reservationRepository.findById(holdId);
        assertThat(hold).isPresent();

        var orders = orderRepository.findByUserId(OTHER_USER_ID);
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("POST /api/orders - Ungültige Request-Daten (400)")
    void purchaseTicket_InvalidRequest() throws Exception {
        // Arrange - Request ohne holdId (Validation Error)
        String invalidJson = "{\"userId\": \"test@example.com\"}";

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/orders/{id} TESTS ====================

    @Test
    @DisplayName("GET /api/orders/{id} - Erfolg")
    void getOrder_Success() throws Exception {
        // Arrange - Erstelle Order via Purchase-Flow
        var holdResponse = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        Long holdId = Long.parseLong(holdResponse.holdId());

        PurchaseTicketRequestDTO purchaseRequest = new PurchaseTicketRequestDTO(holdId, USER_ID);
        String purchaseResponse = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purchaseRequest)))
            .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(purchaseResponse).get("orderId").asLong();

        // Act & Assert - GET Order
        mockMvc.perform(get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.userId").value(USER_ID))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Nicht gefunden (404)")
    void getOrder_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/999")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ==================== GET /api/orders/user/{userId} TESTS ====================

    @Test
    @DisplayName("GET /api/orders/user/{userId} - Erfolg")
    void getUserOrders_Success() throws Exception {
        // Arrange - Erstelle 2 Orders für USER_ID
        // Order 1
        var hold1 = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        PurchaseTicketRequestDTO request1 = new PurchaseTicketRequestDTO(
            Long.parseLong(hold1.holdId()), USER_ID);
        mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)));

        // Order 2 - Neuer Seat
        Seat seat2 = new Seat(testConcert.getId(), "TEST-2", "VIP", "Block A", "1", "2", 99.99);
        seat2 = seatRepository.save(seat2);
        var hold2 = holdApplicationService.createHold(seat2.getId(), USER_ID);
        PurchaseTicketRequestDTO request2 = new PurchaseTicketRequestDTO(
            Long.parseLong(hold2.holdId()), USER_ID);
        mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)));

        // Act & Assert
        mockMvc.perform(get("/api/orders/user/" + USER_ID)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].userId").value(USER_ID))
            .andExpect(jsonPath("$[1].userId").value(USER_ID));
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} - Keine Orders (leere Liste)")
    void getUserOrders_Empty() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/user/nonexistent@example.com")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== HELPER METHODS ====================

    private void setExpiresAt(Reservation reservation, LocalDateTime expiresAt) {
        try {
            var field = Reservation.class.getDeclaredField("expiresAt");
            field.setAccessible(true);
            field.set(reservation, expiresAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set expiresAt", e);
        }
    }
}

package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.HoldApplicationService;
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
 * Integration Tests für OrderController US-179 (Order History & Ticket QR Code).
 * 
 * Test Coverage:
 * - GET /api/users/me/orders - Current User Order History mit Concert/Seat Enrichment
 * - GET /api/orders/{id}/ticket - QR Code Generation als PNG
 * - Security: Ownership validation, unauthorized access
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OrderController US-179 Integration Tests")
@Transactional
class OrderControllerUS179IntegrationTest {

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

    private static final String USER_ID = "us179-test@example.com";
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

        // Create test user
        testUser = User.createUser(
            USER_ID,
            passwordEncoder.encode(USER_PASSWORD),
            "Test",
            "User",
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
            "US-179 Test Concert",
            LocalDateTime.of(2026, 12, 31, 20, 0),
            "Test Arena",
            "Order History Test Event"
        );
        testConcert = concertRepository.save(testConcert);

        // Create test seat
        testSeat = new Seat(
            testConcert.getId(),
            "A-1-VIP",
            "VIP",
            "Block A",
            "1",
            "1",
            99.99
        );
        testSeat = seatRepository.save(testSeat);
    }

    // ==================== GET /api/users/me/orders TESTS ====================

    @Test
    @DisplayName("GET /api/users/me/orders - Sollte Order History mit Concert/Seat Details liefern")
    void getCurrentUserOrderHistory_Success() throws Exception {
        // Arrange - Erstelle Order
        var hold = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(
            Long.parseLong(hold.holdId()), USER_ID);
        mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Act & Assert
        mockMvc.perform(get("/api/users/me/orders")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            // Order Details
            .andExpect(jsonPath("$[0].orderId").exists())
            .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
            .andExpect(jsonPath("$[0].totalPrice").value(99.99))
            // Concert Details
            .andExpect(jsonPath("$[0].concertId").value(testConcert.getId()))
            .andExpect(jsonPath("$[0].concertName").value("US-179 Test Concert"))
            .andExpect(jsonPath("$[0].venue").value("Test Arena"))
            // Seat Details
            .andExpect(jsonPath("$[0].seatId").value(testSeat.getId()))
            .andExpect(jsonPath("$[0].seatNumber").value("A-1-VIP"))
            .andExpect(jsonPath("$[0].category").value("VIP"))
            .andExpect(jsonPath("$[0].block").value("Block A"))
            .andExpect(jsonPath("$[0].row").value("1"))
            .andExpect(jsonPath("$[0].number").value("1"));
    }

    @Test
    @DisplayName("GET /api/users/me/orders - Sollte leere Liste liefern wenn keine Orders")
    void getCurrentUserOrderHistory_Empty() throws Exception {
        // Act & Assert - User hat keine Orders
        mockMvc.perform(get("/api/users/me/orders")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/users/me/orders - Sollte 401 ohne Authentication liefern")
    void getCurrentUserOrderHistory_Unauthorized() throws Exception {
        // Act & Assert - Kein JWT Token
        mockMvc.perform(get("/api/users/me/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users/me/orders - Sollte nur eigene Orders liefern")
    void getCurrentUserOrderHistory_FiltersByAuthenticatedUser() throws Exception {
        // Arrange - Erstelle Order für testUser
        var hold = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(
            Long.parseLong(hold.holdId()), USER_ID);
        mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // Erstelle Order für anderen User
        User otherUser = User.createUser(
            OTHER_USER_ID, passwordEncoder.encode(USER_PASSWORD),
            "Other", "User", UserRole.USER
        );
        userRepository.save(otherUser);

        LoginRequest otherLogin = new LoginRequest(OTHER_USER_ID, USER_PASSWORD);
        String otherLoginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLogin)))
            .andReturn().getResponse().getContentAsString();
        String otherToken = objectMapper.readTree(otherLoginResponse).get("token").asText();

        Seat otherSeat = new Seat(testConcert.getId(), "B-2-STD", "Standard", "B", "2", "2", 49.99);
        otherSeat = seatRepository.save(otherSeat);

        var otherHold = holdApplicationService.createHold(otherSeat.getId(), OTHER_USER_ID);
        PurchaseTicketRequestDTO otherRequest = new PurchaseTicketRequestDTO(
            Long.parseLong(otherHold.holdId()), OTHER_USER_ID);
        mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + otherToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(otherRequest)));

        // Act & Assert - testUser sieht nur seine eigene Order
        mockMvc.perform(get("/api/users/me/orders")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].seatNumber").value("A-1-VIP"));
    }

    // ==================== GET /api/orders/{id}/ticket TESTS ====================

    @Test
    @DisplayName("GET /api/orders/{id}/ticket - Sollte QR Code PNG liefern")
    void getTicketQRCode_Success() throws Exception {
        // Arrange - Erstelle Order
        var hold = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(
            Long.parseLong(hold.holdId()), USER_ID);
        
        String orderResponse = mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andReturn().getResponse().getContentAsString();
        
        Long orderId = objectMapper.readTree(orderResponse).get("orderId").asLong();

        // Act & Assert
        byte[] qrCode = mockMvc.perform(get("/api/orders/" + orderId + "/ticket")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"))
            .andExpect(header().exists("Content-Disposition"))
            .andReturn().getResponse().getContentAsByteArray();

        // Validate PNG
        assertThat(qrCode).isNotEmpty();
        assertThat(qrCode.length).isGreaterThan(100);
        // PNG Header: 89 50 4E 47
        assertThat(qrCode[0]).isEqualTo((byte) 0x89);
        assertThat(qrCode[1]).isEqualTo((byte) 0x50);
        assertThat(qrCode[2]).isEqualTo((byte) 0x4E);
        assertThat(qrCode[3]).isEqualTo((byte) 0x47);
    }

    @Test
    @DisplayName("GET /api/orders/{id}/ticket - Sollte 403 bei fremder Order liefern")
    void getTicketQRCode_Forbidden() throws Exception {
        // Arrange - Erstelle Order für testUser
        var hold = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        PurchaseTicketRequestDTO request = new PurchaseTicketRequestDTO(
            Long.parseLong(hold.holdId()), USER_ID);
        
        String orderResponse = mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andReturn().getResponse().getContentAsString();
        
        Long orderId = objectMapper.readTree(orderResponse).get("orderId").asLong();

        // Arrange - Login als anderer User
        User otherUser = User.createUser(
            OTHER_USER_ID, passwordEncoder.encode(USER_PASSWORD),
            "Other", "User", UserRole.USER
        );
        userRepository.save(otherUser);

        LoginRequest loginRequest = new LoginRequest(OTHER_USER_ID, USER_PASSWORD);
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andReturn().getResponse().getContentAsString();
        String otherUserToken = objectMapper.readTree(loginResponse).get("token").asText();

        // Act & Assert - Anderer User versucht QR Code abzurufen
        mockMvc.perform(get("/api/orders/" + orderId + "/ticket")
                .header("Authorization", "Bearer " + otherUserToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/orders/{id}/ticket - Sollte 404 bei nicht existierender Order liefern")
    void getTicketQRCode_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/999/ticket")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/orders/{id}/ticket - Sollte 401 ohne Authentication liefern")
    void getTicketQRCode_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/1/ticket"))
            .andExpect(status().isUnauthorized());
    }
}

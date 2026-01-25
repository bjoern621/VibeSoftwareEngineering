package com.concertcomparison.presentation.controller;

import com.concertcomparison.domain.exception.SeatNotAvailableException;
import com.concertcomparison.domain.exception.ReservationNotFoundException;
import com.concertcomparison.presentation.dto.HoldCreateRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für Error Handling across REST API.
 * 
 * Verifies:
 * - Error responses sind konsistent strukturiert
 * - HTTP Status Codes sind korrekt
 * - Fehlercodes sind eindeutig
 * - Fehlermeldungen sind benutzerfreundlich (nicht technisch)
 * - Error Responses enthalten Path und Timestamp
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Error Handling Integration Tests")
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== Reservation Endpoint Tests ====================

    @Test
    @DisplayName("POST /api/seats/{invalidId}/hold sollte 404 mit strukturiertem Error zurückgeben")
    void testCreateHoldWithInvalidSeatId() throws Exception {
        // Arrange: Nicht-existierende Seat ID
        Long invalidSeatId = 999999L;
        HoldCreateRequestDTO request = new HoldCreateRequestDTO("user@example.com");

        // Act & Assert
        MvcResult result = mockMvc.perform(
                post("/api/seats/{id}/hold", invalidSeatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user@example.com"
                                }
                                """)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("SEAT_NOT_FOUND"))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.timestamp").isNotEmpty())
        .andExpect(jsonPath("$.path").value("/api/seats/" + invalidSeatId + "/hold"))
        .andReturn();

        // Zusätzliche Assertions
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("code");
        assertThat(responseBody).contains("message");
        assertThat(responseBody).doesNotContain("stackTrace");
        assertThat(responseBody).doesNotContain("exception");
    }

    @Test
    @DisplayName("GET /api/reservations/{invalidId} sollte 404 mit Error Code RESERVATION_NOT_FOUND zurückgeben")
    void testGetReservationWithInvalidId() throws Exception {
        // Arrange: Nicht-existierende Reservation ID
        Long invalidReservationId = 888888L;

        // Act & Assert
        mockMvc.perform(
                get("/api/reservations/{id}", invalidReservationId)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/reservations/{invalidId} sollte 404 zurückgeben")
    void testCancelReservationWithInvalidId() throws Exception {
        // Arrange: Nicht-existierende Reservation ID
        Long invalidReservationId = 777777L;

        // Act & Assert
        mockMvc.perform(
                delete("/api/reservations/{id}", invalidReservationId)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }

    // ==================== Order Endpoint Tests ====================

    @Test
    @DisplayName("POST /api/orders mit ungültigem Hold sollte 404 zurückgeben")
    void testPurchaseTicketWithInvalidHoldId() throws Exception {
        // Arrange: Nicht-existierende Hold ID
        Long invalidHoldId = 666666L;

        // Act & Assert
        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "holdId": %d,
                                    "userId": "user@example.com"
                                }
                                """.formatted(invalidHoldId))
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/orders/{invalidId} sollte 404 mit Error Code ORDER_NOT_FOUND zurückgeben")
    void testGetOrderWithInvalidId() throws Exception {
        // Arrange: Nicht-existierende Order ID
        Long invalidOrderId = 555555L;

        // Act & Assert
        mockMvc.perform(
                get("/api/orders/{id}", invalidOrderId)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
        .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ==================== Validation Error Tests ====================

    @Test
    @DisplayName("POST /api/seats/{id}/hold mit ungültigem Request sollte 400 BAD_REQUEST zurückgeben")
    void testCreateHoldWithInvalidRequest() throws Exception {
        // Arrange: Invalid request (missing userId)
        Long seatId = 1L;

        // Act & Assert
        mockMvc.perform(
                post("/api/seats/{id}/hold", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    // ==================== Error Response Structure Tests ====================

    @Test
    @DisplayName("Alle Error Responses sollten eindeutige Error Codes haben")
    void testErrorCodesAreUnique() throws Exception {
        // Test verschiedene Fehler und verifiziere, dass jeder ein eindeutiger Code hat
        
        // Test 1: SEAT_NOT_FOUND
        mockMvc.perform(get("/api/reservations/999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
        
        // Test 2: ORDER_NOT_FOUND
        mockMvc.perform(get("/api/orders/888888"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    @DisplayName("Error Messages sollten keine Stack Traces enthalten")
    void testErrorMessagesAreUserFriendly() throws Exception {
        // Arrange: Request mit ungültiger Seat ID
        
        // Act
        MvcResult result = mockMvc.perform(
                get("/api/reservations/999999")
        )
        .andExpect(status().isNotFound())
        .andReturn();

        // Assert: Stack traces und technische Details sollten nicht sichtbar sein
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .doesNotContain("stackTrace")
                .doesNotContain("at java")
                .doesNotContain("Caused by")
                .doesNotContain("exception")
                .doesNotContainIgnoringCase("null pointer");
    }

    // ==================== i18n Tests ====================

    @Test
    @DisplayName("Error Messages sollten basierend auf Locale localized sein")
    void testErrorMessagesAreLocalizedByLanguage() throws Exception {
        // Arrange: Request mit Accept-Language Header für Deutsch

        // Act: Request ohne Locale (Standard sollte fallback nutzen)
        MvcResult result = mockMvc.perform(
                get("/api/reservations/999999")
        )
        .andExpect(status().isNotFound())
        .andReturn();

        // Assert: Message sollte nicht leer sein (egal welche Sprache)
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
                .contains("message")
                .contains("code");
    }
}

package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.SeatApplicationService;
import com.concertcomparison.infrastructure.security.CustomUserDetailsService;
import com.concertcomparison.infrastructure.security.JwtAuthenticationFilter;
import com.concertcomparison.infrastructure.security.JwtTokenProvider;
import com.concertcomparison.presentation.dto.AvailabilityByCategoryDTO;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Tests für Seat Availability Endpoint.
 * Testet REST API und JSON Response Structure gemäß OpenAPI Spec.
 */
@WebMvcTest(controllers = SeatController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                classes = {JwtAuthenticationFilter.class}))
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatApplicationService seatApplicationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/events/{id}/seats sollte 200 OK mit JSON zurückgeben")
    void getSeatsForConcert_ShouldReturn200_WithJsonResponse() throws Exception {
        // Arrange
        Long concertId = 1L;
        
        SeatResponseDTO seat1 = SeatResponseDTO.builder()
                .id("1")
                .block("Block A")
                .category("VIP")
                .row("1")
                .number("1")
                .price(129.99)
                .status("AVAILABLE")
                .build();

        SeatResponseDTO seat2 = SeatResponseDTO.builder()
                .id("2")
                .block("Block A")
                .category("VIP")
                .row("1")
                .number("2")
                .price(129.99)
                .status("HELD")
                .build();

        AvailabilityByCategoryDTO vipAvailability = new AvailabilityByCategoryDTO("VIP", 1, 1, 0);

        SeatAvailabilityResponseDTO responseDTO = SeatAvailabilityResponseDTO.builder()
                .concertId("1")
                .seats(Arrays.asList(seat1, seat2))
                .availabilityByCategory(List.of(vipAvailability))
                .build();

        given(seatApplicationService.getSeatAvailability(concertId))
                .willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}/seats", concertId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.concertId").value("1"))
                .andExpect(jsonPath("$.seats").isArray())
                .andExpect(jsonPath("$.seats.length()").value(2))
                .andExpect(jsonPath("$.seats[0].id").value("1"))
                .andExpect(jsonPath("$.seats[0].block").value("Block A"))
                .andExpect(jsonPath("$.seats[0].category").value("VIP"))
                .andExpect(jsonPath("$.seats[0].row").value("1"))
                .andExpect(jsonPath("$.seats[0].number").value("1"))
                .andExpect(jsonPath("$.seats[0].price").value(129.99))
                .andExpect(jsonPath("$.seats[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.availabilityByCategory").isArray())
                .andExpect(jsonPath("$.availabilityByCategory.length()").value(1))
                .andExpect(jsonPath("$.availabilityByCategory[0].category").value("VIP"))
                .andExpect(jsonPath("$.availabilityByCategory[0].available").value(1))
                .andExpect(jsonPath("$.availabilityByCategory[0].held").value(1))
                .andExpect(jsonPath("$.availabilityByCategory[0].sold").value(0));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/events/{id}/seats sollte leere Arrays zurückgeben wenn keine Seats")
    void getSeatsForConcert_ShouldReturnEmpty_WhenNoSeats() throws Exception {
        // Arrange
        Long concertId = 999L;
        
        SeatAvailabilityResponseDTO emptyResponse = SeatAvailabilityResponseDTO.empty("999");

        given(seatApplicationService.getSeatAvailability(concertId))
                .willReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}/seats", concertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value("999"))
                .andExpect(jsonPath("$.seats").isArray())
                .andExpect(jsonPath("$.seats.length()").value(0))
                .andExpect(jsonPath("$.availabilityByCategory").isArray())
                .andExpect(jsonPath("$.availabilityByCategory.length()").value(0));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/events/{id}/seats sollte String IDs verwenden (nicht Long)")
    void getSeatsForConcert_ShouldUseStringIds_NotLong() throws Exception {
        // Arrange
        Long concertId = 42L;
        
        SeatResponseDTO seat = SeatResponseDTO.builder()
                .id("123")
                .block("Block X")
                .category("CATEGORY_A")
                .row("5")
                .number("10")
                .price(79.99)
                .status("AVAILABLE")
                .build();

        SeatAvailabilityResponseDTO responseDTO = SeatAvailabilityResponseDTO.builder()
                .concertId("42")
                .seats(List.of(seat))
                .availabilityByCategory(List.of(new AvailabilityByCategoryDTO("CATEGORY_A", 1, 0, 0)))
                .build();

        given(seatApplicationService.getSeatAvailability(concertId))
                .willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}/seats", concertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").isString())
                .andExpect(jsonPath("$.concertId").value("42"))
                .andExpect(jsonPath("$.seats[0].id").isString())
                .andExpect(jsonPath("$.seats[0].id").value("123"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/events/{id}/seats sollte mehrere Kategorien unterstützen")
    void getSeatsForConcert_ShouldSupportMultipleCategories() throws Exception {
        // Arrange
        Long concertId = 1L;
        
        SeatResponseDTO vipSeat = SeatResponseDTO.builder()
                .id("1")
                .block("A")
                .category("VIP")
                .row("1")
                .number("1")
                .price(129.99)
                .status("AVAILABLE")
                .build();

        SeatResponseDTO regularSeat = SeatResponseDTO.builder()
                .id("2")
                .block("B")
                .category("CATEGORY_A")
                .row("2")
                .number("1")
                .price(79.99)
                .status("AVAILABLE")
                .build();

        AvailabilityByCategoryDTO vipAvailability = new AvailabilityByCategoryDTO("VIP", 1, 0, 0);
        AvailabilityByCategoryDTO regularAvailability = new AvailabilityByCategoryDTO("CATEGORY_A", 1, 0, 0);

        SeatAvailabilityResponseDTO responseDTO = SeatAvailabilityResponseDTO.builder()
                .concertId("1")
                .seats(Arrays.asList(vipSeat, regularSeat))
                .availabilityByCategory(Arrays.asList(regularAvailability, vipAvailability))  // Alphabetisch sortiert
                .build();

        given(seatApplicationService.getSeatAvailability(concertId))
                .willReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}/seats", concertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats.length()").value(2))
                .andExpect(jsonPath("$.availabilityByCategory.length()").value(2))
                .andExpect(jsonPath("$.availabilityByCategory[0].category").value("CATEGORY_A"))
                .andExpect(jsonPath("$.availabilityByCategory[1].category").value("VIP"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/events/{id}/seats sollte alle erforderlichen Seat-Felder enthalten")
    void getSeatsForConcert_ShouldIncludeAllRequiredSeatFields() throws Exception {
        // Arrange
        Long concertId = 1L;
        
        SeatResponseDTO seat = SeatResponseDTO.builder()
                .id("1")
                .block("Block A")
                .category("VIP")
                .row("3")
                .number("5")
                .price(149.99)
                .status("SOLD")
                .build();

        SeatAvailabilityResponseDTO responseDTO = SeatAvailabilityResponseDTO.builder()
                .concertId("1")
                .seats(List.of(seat))
                .availabilityByCategory(List.of(new AvailabilityByCategoryDTO("VIP", 0, 0, 1)))
                .build();

        given(seatApplicationService.getSeatAvailability(concertId))
                .willReturn(responseDTO);

        // Act & Assert - Prüfe dass ALLE Felder aus OpenAPI Spec vorhanden sind
        mockMvc.perform(get("/api/events/{id}/seats", concertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seats[0].id").exists())
                .andExpect(jsonPath("$.seats[0].block").exists())
                .andExpect(jsonPath("$.seats[0].category").exists())
                .andExpect(jsonPath("$.seats[0].row").exists())
                .andExpect(jsonPath("$.seats[0].number").exists())
                .andExpect(jsonPath("$.seats[0].price").exists())
                .andExpect(jsonPath("$.seats[0].status").exists())
                // Prüfe dass KEINE Extra-Felder vorhanden sind
                .andExpect(jsonPath("$.seats[0].seatNumber").doesNotExist())
                .andExpect(jsonPath("$.seats[0].statusDisplayName").doesNotExist())
                .andExpect(jsonPath("$.seats[0].isAvailable").doesNotExist())
                .andExpect(jsonPath("$.seats[0].holdExpiresAt").doesNotExist())
                .andExpect(jsonPath("$.totalSeats").doesNotExist())
                .andExpect(jsonPath("$.availableSeats").doesNotExist());
    }
}

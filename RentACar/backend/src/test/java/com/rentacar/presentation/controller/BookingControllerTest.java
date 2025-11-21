package com.rentacar.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentacar.TestSecurityConfig;
import com.rentacar.application.service.BookingApplicationService;
import com.rentacar.infrastructure.security.JwtAuthenticationFilter;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.infrastructure.security.CustomerUserDetailsService;
import com.rentacar.presentation.dto.PriceCalculationRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für BookingController.
 */
@WebMvcTest(controllers = BookingController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {JwtAuthenticationFilter.class})
})
@Import({TestSecurityConfig.class, BookingApplicationService.class})
class BookingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BookingApplicationService bookingApplicationService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private CustomerUserDetailsService customerUserDetailsService;
    
    @Test
    void shouldCalculatePriceSuccessfully() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(7);
        LocalDateTime returnDate = pickup.plusDays(3);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "COMPACT_CAR",
            pickup,
            returnDate,
            Arrays.asList("GPS", "CHILD_SEAT")
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void shouldReturnBadRequestWhenVehicleTypeIsNull() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(7);
        LocalDateTime returnDate = pickup.plusDays(3);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            null,  // Ungültig
            pickup,
            returnDate,
            Arrays.asList()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnBadRequestWhenPickupDateTimeIsNull() throws Exception {
        // Arrange
        LocalDateTime returnDate = LocalDateTime.now().plusDays(10);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "SEDAN",
            null,  // Ungültig
            returnDate,
            Arrays.asList()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnBadRequestWhenReturnDateTimeIsNull() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(7);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "SUV",
            pickup,
            null,  // Ungültig
            Arrays.asList()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldReturnBadRequestWhenPickupIsInPast() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().minusDays(1);  // In der Vergangenheit
        LocalDateTime returnDate = LocalDateTime.now().plusDays(3);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "VAN",
            pickup,
            returnDate,
            Arrays.asList()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldCalculatePriceWithoutAdditionalServices() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(5);
        LocalDateTime returnDate = pickup.plusDays(2);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "SEDAN",
            pickup,
            returnDate,
            Arrays.asList()  // Keine Zusatzleistungen
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    @Test
    void shouldCalculatePriceWithMultipleAdditionalServices() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(10);
        LocalDateTime returnDate = pickup.plusDays(7);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "SUV",
            pickup,
            returnDate,
            Arrays.asList("GPS", "CHILD_SEAT", "FULL_INSURANCE", "ADDITIONAL_DRIVER")
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    @Test
    void shouldReturnBadRequestForInvalidVehicleType() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(5);
        LocalDateTime returnDate = pickup.plusDays(2);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "INVALID_TYPE",  // Ungültiger Fahrzeugtyp
            pickup,
            returnDate,
            Arrays.asList()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldReturnBadRequestForInvalidAdditionalService() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(5);
        LocalDateTime returnDate = pickup.plusDays(2);
        
        PriceCalculationRequestDTO request = new PriceCalculationRequestDTO(
            "COMPACT_CAR",
            pickup,
            returnDate,
            Arrays.asList("INVALID_SERVICE")  // Ungültige Zusatzleistung
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldAcceptRequestWithNullAdditionalServices() throws Exception {
        // Arrange
        LocalDateTime pickup = LocalDateTime.now().plusDays(5).withNano(0);
        LocalDateTime returnDate = pickup.plusDays(2).withNano(0);
        
        String requestJson = String.format(
            "{\"vehicleType\":\"COMPACT_CAR\",\"pickupDateTime\":\"%s\",\"returnDateTime\":\"%s\"}",
            pickup.toString(),
            returnDate.toString()
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/buchungen/preis-berechnen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());
    }
}

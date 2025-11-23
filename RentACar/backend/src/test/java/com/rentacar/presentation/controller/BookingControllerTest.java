package com.rentacar.presentation.controller;

import com.rentacar.domain.exception.VehicleNotAvailableException;
import com.rentacar.domain.model.*;
import com.rentacar.presentation.dto.CreateBookingRequestDTO;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;
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
    
    @MockBean
    private com.rentacar.domain.repository.BookingRepository bookingRepository;
    
    @MockBean
    private com.rentacar.domain.repository.CustomerRepository customerRepository;

    @MockBean
    private com.rentacar.domain.service.BookingDomainService bookingDomainService;

    @MockBean
    private com.rentacar.domain.repository.VehicleRepository vehicleRepository;

    @MockBean
    private com.rentacar.domain.repository.BranchRepository branchRepository;
    
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
    
    @Test
    void shouldReturnConflictWhenVehicleNotAvailable() throws Exception {
        // Arrange
        CreateBookingRequestDTO request = new CreateBookingRequestDTO();
        request.setVehicleId(1L);
        request.setPickupBranchId(1L);
        request.setReturnBranchId(1L);
        request.setPickupDateTime(LocalDateTime.now().plusDays(1));
        request.setReturnDateTime(LocalDateTime.now().plusDays(3));
        request.setAdditionalServices(Collections.emptyList());

        Branch branch = new Branch("Branch 1", "Street, City", "9-17");
        ReflectionTestUtils.setField(branch, "id", 1L);

        Vehicle vehicle = new Vehicle(
            LicensePlate.of("M-XY 123"),
            "BMW", "X1", 2023,
            Mileage.of(1000),
            VehicleType.COMPACT_CAR,
            branch
        );
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        Vehicle alternative = new Vehicle(
            LicensePlate.of("M-XY 999"),
            "BMW", "X1", 2023,
            Mileage.of(1000),
            VehicleType.COMPACT_CAR,
            branch
        );
        ReflectionTestUtils.setField(alternative, "id", 2L);

        // Mock dependencies
        Customer customer = org.mockito.Mockito.mock(Customer.class);
        when(customerRepository.findById(any())).thenReturn(java.util.Optional.of(customer));
        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));
        when(branchRepository.findById(1L)).thenReturn(java.util.Optional.of(branch));

        when(bookingDomainService.createBooking(any(), any(), any(), any(), any(), any(), any()))
            .thenThrow(new VehicleNotAvailableException("Not available", Collections.singletonList(alternative)));

        // Act & Assert
        mockMvc.perform(post("/api/buchungen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Not available"))
                .andExpect(jsonPath("$.alternativeVehicles[0].id").value(2));
    }
}

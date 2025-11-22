package com.rentacar.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentacar.TestSecurityConfig;
import com.rentacar.application.service.BookingApplicationService;
import com.rentacar.domain.model.*;
import com.rentacar.infrastructure.security.JwtAuthenticationFilter;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.infrastructure.security.CustomerUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für Buchungshistorie-Endpoints.
 * 
 * Testet:
 * - GET /api/kunden/meine-buchungen (Customer sieht eigene Buchungen)
 * - GET /api/kunden/{id}/buchungen (Employee/Admin sieht alle Buchungen)
 * - Filter nach Status
 * - Chronologische Sortierung (neueste zuerst)
 */
@WebMvcTest(controllers = BookingController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {JwtAuthenticationFilter.class})
})
@Import(TestSecurityConfig.class)
class BookingHistoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private BookingApplicationService bookingApplicationService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private CustomerUserDetailsService customerUserDetailsService;
    
    @MockBean
    private com.rentacar.domain.repository.BookingRepository bookingRepository;
    
    @MockBean
    private com.rentacar.domain.repository.CustomerRepository customerRepository;
    
    @Test
    void shouldGetMyBookings() throws Exception {
        // Arrange
        Long customerId = 1L;
        
        // Mock JWT extraction - es wird KEIN Authentication-Objekt übergeben wegen TestSecurityConfig
        // Der Controller ruft jwtUtil.extractCustomerId(authentication) auf, aber authentication ist null im Test
        // Daher mocken wir null-Input
        when(jwtUtil.extractCustomerId((Authentication) null)).thenReturn(customerId);
        
        List<Booking> bookings = createTestBookings(customerId);
        when(bookingApplicationService.getCustomerBookingsByStatus(eq(customerId), eq(null)))
            .thenReturn(bookings);
        
        // Act & Assert
        mockMvc.perform(get("/api/kunden/meine-buchungen"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].buchungsnummer").value(2L))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[1].buchungsnummer").value(1L))
                .andExpect(jsonPath("$[1].status").value("REQUESTED"));
    }
    
    @Test
    void shouldGetMyBookingsFilteredByStatus() throws Exception {
        // Arrange
        Long customerId = 1L;
        when(jwtUtil.extractCustomerId((Authentication) null)).thenReturn(customerId);
        
        List<Booking> confirmedBookings = List.of(createTestBookings(customerId).get(0)); // Index 0 ist CONFIRMED (neueste)
        when(bookingApplicationService.getCustomerBookingsByStatus(eq(customerId), eq(BookingStatus.CONFIRMED)))
            .thenReturn(confirmedBookings);
        
        // Act & Assert
        mockMvc.perform(get("/api/kunden/meine-buchungen")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }
    
    @Test
    void shouldGetCustomerBookingsAsEmployee() throws Exception {
        // Arrange
        Long customerId = 1L;
        List<Booking> bookings = createTestBookings(customerId);
        when(bookingApplicationService.getCustomerBookingsByStatus(eq(customerId), eq(null)))
            .thenReturn(bookings);
        
        // Act & Assert
        mockMvc.perform(get("/api/kunden/{id}/buchungen", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void shouldGetCustomerBookingsFilteredByStatusAsEmployee() throws Exception {
        // Arrange
        Long customerId = 1L;
        List<Booking> cancelledBookings = List.of(createCancelledBooking(customerId));
        when(bookingApplicationService.getCustomerBookingsByStatus(eq(customerId), eq(BookingStatus.CANCELLED)))
            .thenReturn(cancelledBookings);
        
        // Act & Assert
        mockMvc.perform(get("/api/kunden/{id}/buchungen", customerId)
                .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$[0].stornierungsgrund").value("Kunde hat storniert"));
    }
    
    @Test
    void shouldReturnEmptyListWhenNoBookings() throws Exception {
        // Arrange
        Long customerId = 1L;
        when(jwtUtil.extractCustomerId((Authentication) null)).thenReturn(customerId);
        when(bookingApplicationService.getCustomerBookingsByStatus(eq(customerId), eq(null)))
            .thenReturn(List.of());
        
        // Act & Assert
        mockMvc.perform(get("/api/kunden/meine-buchungen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    void shouldIncludeAllRequiredFieldsInResponse() throws Exception {
        // Arrange
        Long customerId = 1L;
        when(jwtUtil.extractCustomerId((Authentication) null)).thenReturn(customerId);
        
        List<Booking> bookings = createTestBookings(customerId);
        when(bookingApplicationService.getCustomerBookingsByStatus(eq(customerId), eq(null)))
            .thenReturn(bookings);
        
        // Act & Assert - Prüfe alle Pflichtfelder aus den Akzeptanzkriterien
        mockMvc.perform(get("/api/kunden/meine-buchungen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buchungsnummer").exists())
                .andExpect(jsonPath("$[0].fahrzeug.kennzeichen").exists())
                .andExpect(jsonPath("$[0].fahrzeug.marke").exists())
                .andExpect(jsonPath("$[0].fahrzeug.modell").exists())
                .andExpect(jsonPath("$[0].abholdatum").exists())
                .andExpect(jsonPath("$[0].rueckgabedatum").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].gesamtpreis").exists())
                .andExpect(jsonPath("$[0].waehrung").value("EUR"));
    }
    
    /**
     * Hilfsmethode: Erstellt Test-Buchungen für einen Kunden.
     * Chronologisch sortiert (neueste zuerst).
     */
    private List<Booking> createTestBookings(Long customerId) {
        Customer customer = createTestCustomer(customerId);
        Vehicle vehicle1 = createTestVehicle(1L, "VW", "Golf");
        Vehicle vehicle2 = createTestVehicle(2L, "BMW", "3er");
        Branch branch = createTestBranch();
        
        Booking booking1 = createBooking(1L, customer, vehicle1, branch, BookingStatus.REQUESTED, 
            LocalDateTime.now().minusDays(5));
        Booking booking2 = createBooking(2L, customer, vehicle2, branch, BookingStatus.CONFIRMED,
            LocalDateTime.now().minusDays(2));
        
        // Neueste zuerst!
        return Arrays.asList(booking2, booking1);
    }
    
    private Booking createCancelledBooking(Long customerId) {
        Customer customer = createTestCustomer(customerId);
        Vehicle vehicle = createTestVehicle(1L, "VW", "Golf");
        Branch branch = createTestBranch();
        
        Booking booking = createBooking(3L, customer, vehicle, branch, BookingStatus.CANCELLED,
            LocalDateTime.now().minusDays(10));
        // Reflection verwenden um cancellationReason zu setzen (da cancel() Methode Validierung hat)
        try {
            var field = Booking.class.getDeclaredField("cancellationReason");
            field.setAccessible(true);
            field.set(booking, "Kunde hat storniert");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return booking;
    }
    
    private Booking createBooking(Long id, Customer customer, Vehicle vehicle, Branch branch, 
                                  BookingStatus status, LocalDateTime createdAt) {
        LocalDateTime pickup = LocalDateTime.now().plusDays(7);
        LocalDateTime returnDate = pickup.plusDays(3);
        
        // Booking-Konstruktor ohne Currency-Parameter
        Booking booking = new Booking(
            customer,
            vehicle,
            branch,
            branch,
            pickup,
            returnDate,
            BigDecimal.valueOf(250.00),
            new HashSet<>()
        );
        
        // Set ID und createdAt via Reflection (da JPA diese normalerweise setzt)
        try {
            var idField = Booking.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(booking, id);
            
            var createdAtField = Booking.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(booking, createdAt);
            
            var statusField = Booking.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(booking, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return booking;
    }
    
    private Customer createTestCustomer(Long id) {
        Customer customer = new Customer(
            "Max",
            "Mustermann",
            new Address("Hauptstraße 1", "12345", "Berlin"),
            new DriverLicenseNumber("B12345678AB"),
            "max@example.com",
            "0301234567"
        );
        
        try {
            var field = Customer.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(customer, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return customer;
    }
    
    private Vehicle createTestVehicle(Long id, String brand, String model) {
        Vehicle vehicle = new Vehicle(
            LicensePlate.of("B-AB 1234"),
            brand,
            model,
            2023,
            Mileage.of(10000),
            VehicleType.COMPACT_CAR,
            createTestBranch()
        );
        
        try {
            var field = Vehicle.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(vehicle, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return vehicle;
    }
    
    private Branch createTestBranch() {
        Branch branch = new Branch(
            "Berlin Mitte",
            "Unter den Linden 1, 10117 Berlin",
            "Mo-Fr 08:00-18:00"
        );
        
        try {
            var field = Branch.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(branch, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return branch;
    }
}

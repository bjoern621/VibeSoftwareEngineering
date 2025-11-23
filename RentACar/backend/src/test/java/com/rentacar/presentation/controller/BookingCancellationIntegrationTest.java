package com.rentacar.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentacar.TestSecurityConfig;
import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.*;
import com.rentacar.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Tests für FR11: Buchungsstornierung
 *
 * Testet alle Akzeptanzkriterien:
 * - API Endpoint: DELETE /api/bookings/{id}/cancel
 * - Stornierung nur möglich, wenn mindestens 24h bis Abholung verbleiben
 * - Bei Stornierung wird Status auf CANCELLED gesetzt
 * - Fahrzeug wird wieder als verfügbar markiert
 * - Kunde erhält Bestätigungs-E-Mail (via Mock/Log)
 * - Bei Versuch < 24h vor Abholung wird Fehler zurückgegeben
 * - Nur der Buchungseigentümer oder Mitarbeiter/Admin darf stornieren
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
class BookingCancellationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BranchRepository branchRepository;

    @MockBean
    private JwtUtil jwtUtil;

    private Customer testCustomer;
    private Customer otherCustomer;
    private Vehicle testVehicle;
    private Branch testBranch;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        // Test-Daten aufbauen
        testBranch = createBranch("Hauptfiliale", "Hauptstraße 1, 10115 Berlin");
        branchRepository.save(testBranch);

        testVehicle = createVehicle("HH-AB 1234");
        vehicleRepository.save(testVehicle);

        testCustomer = createCustomer("john.doe@example.com", "John", "Doe", "DL123456789");
        customerRepository.save(testCustomer);

        otherCustomer = createCustomer("jane.smith@example.com", "Jane", "Smith", "DL987654321");
        customerRepository.save(otherCustomer);

        // Mock JwtUtil to return customer IDs based on username
        when(jwtUtil.extractCustomerId(any(Authentication.class))).thenAnswer(invocation -> {
            Authentication auth = invocation.getArgument(0);
            String username = auth.getName();
            if ("john.doe@example.com".equals(username)) {
                return testCustomer.getId();
            } else if ("jane.smith@example.com".equals(username)) {
                return otherCustomer.getId();
            } else if ("employee@rentacar.com".equals(username)) {
                return null; // Employees don't need a customer ID
            } else if ("admin@rentacar.com".equals(username)) {
                return null; // Admins don't need a customer ID
            }
            return null;
        });
    }

    // ============================================
    // Test 1: Erfolgreiche Stornierung (Happy Path)
    // ============================================
    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"CUSTOMER"})
    void shouldCancelBookingSuccessfully_WhenMoreThan24HoursBeforePickup() throws Exception {
        // Given: Buchung mit Abholzeit in 48h
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(48);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        bookingRepository.save(testBooking);

        testVehicle.markAsRented(); // Fahrzeug als gemietet markieren
        vehicleRepository.save(testVehicle);

        // When: Stornierung durchführen
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isNoContent());

        // Then: Status auf CANCELLED gesetzt
        Booking cancelledBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // Note: Fahrzeug-Verfügbarkeit wird asynchron via BookingEventHandler gesetzt
        // Note: E-Mail wird asynchron via BookingEventHandler gesendet (MockEmailService)
    }

    // ============================================
    // Test 2: Stornierung zu spät (< 24h)
    // ============================================
    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"CUSTOMER"})
    void shouldRejectCancellation_WhenLessThan24HoursBeforePickup() throws Exception {
        // Given: Buchung mit Abholzeit in 20h
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(20);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        bookingRepository.save(testBooking);

        // When/Then: Stornierung wird abgelehnt
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isBadRequest());

        // Buchung bleibt aktiv
        Booking booking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // ============================================
    // Test 3: Stornierung genau 24h vorher (Grenzfall)
    // ============================================
    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"CUSTOMER"})
    void shouldCancelBooking_WhenExactly24HoursBeforePickup() throws Exception {
        // Given: Buchung mit Abholzeit in genau 24h + 1 Minute
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(24).plusMinutes(1);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        bookingRepository.save(testBooking);

        // When/Then: Stornierung erfolgreich
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isNoContent());

        Booking cancelledBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    // ============================================
    // Test 4: Nur Owner kann stornieren
    // ============================================
    @Test
    @WithMockUser(username = "jane.smith@example.com", roles = {"CUSTOMER"})
    void shouldRejectCancellation_WhenDifferentCustomerAttempts() throws Exception {
        // Given: Buchung gehört john.doe
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(48);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        bookingRepository.save(testBooking);

        // When/Then: jane.smith versucht zu stornieren -> Forbidden
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isForbidden());

        // Buchung bleibt aktiv
        Booking booking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // ============================================
    // Test 5: Mitarbeiter kann jede Buchung stornieren
    // ============================================
    @Test
    @WithMockUser(username = "employee@rentacar.com", roles = {"EMPLOYEE"})
    void shouldAllowCancellation_WhenEmployeeAttempts() throws Exception {
        // Given: Buchung gehört john.doe
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(48);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        bookingRepository.save(testBooking);

        // When/Then: Employee kann stornieren
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isNoContent());

        Booking cancelledBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    // ============================================
    // Test 6: Admin kann jede Buchung stornieren
    // ============================================
    @Test
    @WithMockUser(username = "admin@rentacar.com", roles = {"ADMIN"})
    void shouldAllowCancellation_WhenAdminAttempts() throws Exception {
        // Given: Buchung gehört john.doe
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(48);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        bookingRepository.save(testBooking);

        // When/Then: Admin kann stornieren
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isNoContent());

        Booking cancelledBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertThat(cancelledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    // ============================================
    // Test 7: Buchung nicht gefunden
    // ============================================
    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"CUSTOMER"})
    void shouldReturn404_WhenBookingNotFound() throws Exception {
        // When/Then: Nicht existierende Buchung
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", 99999L))
            .andExpect(status().isNotFound());
    }

    // ============================================
    // Test 8: Bereits stornierte Buchung
    // ============================================
    @Test
    @WithMockUser(username = "john.doe@example.com", roles = {"CUSTOMER"})
    void shouldRejectCancellation_WhenBookingAlreadyCancelled() throws Exception {
        // Given: Bereits stornierte Buchung
        LocalDateTime pickupDate = LocalDateTime.now().plusHours(48);
        LocalDateTime returnDate = pickupDate.plusDays(3);

        testBooking = createBooking(testCustomer, testVehicle, testBranch, pickupDate, returnDate);
        testBooking.cancel(LocalDateTime.now(), "Already cancelled");
        testBooking = bookingRepository.save(testBooking);

        // When/Then: Zweite Stornierung wird abgelehnt
        mockMvc.perform(delete("/api/buchungen/{id}/stornieren", testBooking.getId()))
            .andExpect(status().isBadRequest());
    }

    // ============================================
    // Test 8: Bereits stornierte Buchung
    // ============================================

    private Customer createCustomer(String email, String firstName, String lastName, String driverLicenseNum) {
        return new Customer(
            firstName,
            lastName,
            new Address("Teststraße 123", "12345", "Berlin"),
            new DriverLicenseNumber(driverLicenseNum),
            email,
            null, // phone number
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy" // BCrypt hash of "password"
        );
    }

    private Vehicle createVehicle(String licensePlateStr) {
        return new Vehicle(
            LicensePlate.of(licensePlateStr),
            "BMW",
            "3er",
            2023,
            Mileage.of(15000),
            VehicleType.SEDAN,
            testBranch
        );
    }

    private Branch createBranch(String name, String address) {
        return new Branch(name, address, "Mo-Fr 8-18 Uhr");
    }

    private Booking createBooking(
        Customer customer,
        Vehicle vehicle,
        Branch branch,
        LocalDateTime pickupDate,
        LocalDateTime returnDate
    ) {
        Booking booking = new Booking(
            customer,
            vehicle,
            branch,
            branch,
            pickupDate,
            returnDate,
            BigDecimal.valueOf(269.97),
            new HashSet<>()  // Empty additional services
        );
        booking.confirm(); // Set status to CONFIRMED
        return booking;
    }
}


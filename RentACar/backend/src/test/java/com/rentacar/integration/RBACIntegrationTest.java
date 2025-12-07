package com.rentacar.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentacar.domain.model.Customer;
import com.rentacar.domain.model.Role;
import com.rentacar.domain.repository.CustomerRepository;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.presentation.dto.CreateVehicleRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration-Tests für rollenbasierte Zugriffskontrolle (RBAC).
 * 
 * Testet die @PreAuthorize-Annotationen und Guards in den Controllern.
 * Verwendet echte Spring Security Context mit JWT-Tokens.
 * 
 * @see com.rentacar.infrastructure.security.RoleConstants
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("RBAC Integration Tests")
class RBACIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerToken;
    private String employeeToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Test-User erstellen (falls nicht bereits vorhanden)
        createTestUserIfNotExists("rbac.customer@test.com", Role.CUSTOMER);
        createTestUserIfNotExists("rbac.employee@test.com", Role.EMPLOYEE);
        createTestUserIfNotExists("rbac.admin@test.com", Role.ADMIN);

        // JWT-Tokens generieren
        Customer customer = customerRepository.findByEmail("rbac.customer@test.com").orElseThrow();
        Customer employee = customerRepository.findByEmail("rbac.employee@test.com").orElseThrow();
        Customer admin = customerRepository.findByEmail("rbac.admin@test.com").orElseThrow();

        customerToken = jwtUtil.generateToken(customer.getEmail(), customer.getId(), customer.getRole());
        employeeToken = jwtUtil.generateToken(employee.getEmail(), employee.getId(), employee.getRole());
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getId(), admin.getRole());
    }

    private void createTestUserIfNotExists(String email, Role role) {
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer user = new Customer(
                "Test",
                "User",
                email,
                passwordEncoder.encode("Test1234!"),
                "Test Address",
                "12345",
                "Berlin",
                "0123456789",
                "DE12345678",
                role
            );
            user.setEmailVerified(true); // Email direkt verifiziert
            customerRepository.save(user);
        }
    }

    // ==================== Vehicle Management Tests ====================

    @Test
    @DisplayName("CUSTOMER cannot create vehicle (403 Forbidden)")
    void customerCannotCreateVehicle() throws Exception {
        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
            "B-RB 1111",
            "Test",
            "Model",
            2024,
            0,
            "SUV",
            1L
        );

        mockMvc.perform(post("/api/fahrzeuge")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Zugriff verweigert"));
    }

    @Test
    @DisplayName("EMPLOYEE can create vehicle (201 Created)")
    void employeeCanCreateVehicle() throws Exception {
        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
            "B-EM 2222",
            "BMW",
            "X5",
            2024,
            0,
            "SUV",
            1L
        );

        mockMvc.perform(post("/api/fahrzeuge")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.licensePlate").value("B-EM 2222"));
    }

    @Test
    @DisplayName("ADMIN can create vehicle (201 Created)")
    void adminCanCreateVehicle() throws Exception {
        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
            "M-AD 3333",
            "Mercedes",
            "S-Class",
            2024,
            0,
            "SEDAN",
            1L
        );

        mockMvc.perform(post("/api/fahrzeuge")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.licensePlate").value("M-AD 3333"));
    }

    @Test
    @DisplayName("EMPLOYEE can update vehicle (200 OK)")
    void employeeCanUpdateVehicle() throws Exception {
        // Erstelle Fahrzeug als Employee
        CreateVehicleRequestDTO createRequest = new CreateVehicleRequestDTO(
            "B-UP 4444",
            "VW",
            "Golf",
            2023,
            5000,
            "COMPACT_CAR",
            1L
        );

        String createResponse = mockMvc.perform(post("/api/fahrzeuge")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long vehicleId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update mit neuem Kilometerstand
        String updatePayload = """
            {
                "mileage": 10000,
                "branchId": 1
            }
            """;

        mockMvc.perform(put("/api/fahrzeuge/" + vehicleId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mileage").value(10000));
    }

    @Test
    @DisplayName("CUSTOMER cannot update vehicle (403 Forbidden)")
    void customerCannotUpdateVehicle() throws Exception {
        String updatePayload = """
            {
                "mileage": 99999,
                "branchId": 1
            }
            """;

        mockMvc.perform(put("/api/fahrzeuge/1")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
            .andExpect(status().isForbidden());
    }

    // ==================== Booking Tests ====================

    @Test
    @DisplayName("CUSTOMER can create booking (201 Created)")
    void customerCanCreateBooking() throws Exception {
        String bookingPayload = """
            {
                "vehicleId": 1,
                "pickupBranchId": 1,
                "returnBranchId": 1,
                "pickupDateTime": "2026-07-01T10:00:00",
                "returnDateTime": "2026-07-05T10:00:00",
                "options": []
            }
            """;

        mockMvc.perform(post("/api/buchungen")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingPayload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fahrzeugId").value(1));
    }

    @Test
    @DisplayName("EMPLOYEE cannot create booking (403 Forbidden)")
    void employeeCannotCreateBooking() throws Exception {
        String bookingPayload = """
            {
                "vehicleId": 1,
                "pickupBranchId": 1,
                "returnBranchId": 1,
                "pickupDateTime": "2026-08-01T10:00:00",
                "returnDateTime": "2026-08-05T10:00:00",
                "options": []
            }
            """;

        mockMvc.perform(post("/api/buchungen")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingPayload))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Zugriff verweigert"));
    }

    @Test
    @DisplayName("ADMIN cannot create booking (403 Forbidden)")
    void adminCannotCreateBooking() throws Exception {
        String bookingPayload = """
            {
                "vehicleId": 1,
                "pickupBranchId": 1,
                "returnBranchId": 1,
                "pickupDateTime": "2026-09-01T10:00:00",
                "returnDateTime": "2026-09-05T10:00:00",
                "options": []
            }
            """;

        mockMvc.perform(post("/api/buchungen")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingPayload))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE can view all bookings (200 OK)")
    void employeeCanViewAllBookings() throws Exception {
        mockMvc.perform(get("/api/buchungen")
                .header("Authorization", "Bearer " + employeeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CUSTOMER cannot view all bookings (403 Forbidden)")
    void customerCannotViewAllBookings() throws Exception {
        mockMvc.perform(get("/api/buchungen")
                .header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isForbidden());
    }

    // ==================== Damage Report Tests ====================

    @Test
    @DisplayName("EMPLOYEE can access damage reports (200 OK)")
    void employeeCanAccessDamageReports() throws Exception {
        mockMvc.perform(get("/api/buchungen/1/schadensberichte")
                .header("Authorization", "Bearer " + employeeToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CUSTOMER cannot access damage reports (403 Forbidden)")
    void customerCannotAccessDamageReports() throws Exception {
        mockMvc.perform(get("/api/buchungen/1/schadensberichte")
                .header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Zugriff verweigert"));
    }

    @Test
    @DisplayName("ADMIN can access damage reports (200 OK)")
    void adminCanAccessDamageReports() throws Exception {
        mockMvc.perform(get("/api/buchungen/1/schadensberichte")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    // ==================== Public Endpoint Tests ====================

    @Test
    @DisplayName("Public endpoint accessible without authentication")
    void publicEndpointAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/fahrzeuge"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void protectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/fahrzeuge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ==================== Role Equivalence Tests ====================

    @Test
    @DisplayName("ADMIN has same permissions as EMPLOYEE (extended staff rights)")
    void adminHasSamePermissionsAsEmployee() throws Exception {
        // Test 1: Beide können Fahrzeuge erstellen
        CreateVehicleRequestDTO request = new CreateVehicleRequestDTO(
            "B-EQ 5555",
            "Audi",
            "A4",
            2024,
            0,
            "SEDAN",
            1L
        );

        mockMvc.perform(post("/api/fahrzeuge")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        CreateVehicleRequestDTO adminRequest = new CreateVehicleRequestDTO(
            "M-EQ 6666",
            "Audi",
            "A6",
            2024,
            0,
            "SEDAN",
            1L
        );

        mockMvc.perform(post("/api/fahrzeuge")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)))
            .andExpect(status().isCreated());

        // Test 2: Beide können Schadensberichte sehen
        mockMvc.perform(get("/api/buchungen/1/schadensberichte")
                .header("Authorization", "Bearer " + employeeToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/buchungen/1/schadensberichte")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());

        // Test 3: Beide können NICHT Buchungen erstellen
        String bookingPayload = """
            {
                "vehicleId": 1,
                "pickupBranchId": 1,
                "returnBranchId": 1,
                "pickupDateTime": "2026-10-01T10:00:00",
                "returnDateTime": "2026-10-05T10:00:00",
                "options": []
            }
            """;

        mockMvc.perform(post("/api/buchungen")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingPayload))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/buchungen")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingPayload))
            .andExpect(status().isForbidden());
    }
}

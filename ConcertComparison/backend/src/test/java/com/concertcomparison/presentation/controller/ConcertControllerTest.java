package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.ConcertApplicationService;
import com.concertcomparison.domain.exception.InvalidConcertDateException;
import com.concertcomparison.presentation.dto.CreateConcertRequestDTO;
import com.concertcomparison.presentation.dto.CreateSeatRequestDTO;
import com.concertcomparison.presentation.dto.CreateSeatsRequestDTO;
import com.concertcomparison.presentation.dto.ConcertResponseDTO;
import com.concertcomparison.presentation.dto.UpdateConcertRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.concertcomparison.infrastructure.security.JwtTokenProvider;
import com.concertcomparison.infrastructure.security.CustomUserDetailsService;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für ConcertController.
 * 
 * Test-Struktur:
 * - @WebMvcTest für schnelle Controller-Tests
 * - MockMvc für HTTP Request Simulation
 * - @WithMockUser für Authentifizierung
 * - MockBean für Service-Mocking
 * 
 * Getestete Szenarien (US-09):
 * - Authentifizierung und Authorization (403 für non-admin)
 * - POST /api/concerts (201 Created, 400 Bad Request)
 * - PUT /api/concerts/{id} (200 OK, 404 Not Found, 409 Conflict)
 * - DELETE /api/concerts/{id} (204 No Content)
 * - POST /api/concerts/{id}/seats (201 Created)
 * - GET /api/concerts (200 OK, public endpoint)
 */
import com.concertcomparison.infrastructure.security.TestSecurityConfig;
import com.concertcomparison.presentation.exception.GlobalExceptionHandler;

@WebMvcTest(ConcertController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("ConcertController Integration Tests")
class ConcertControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ConcertApplicationService concertApplicationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    
    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusMonths(1);
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    
    @Nested
    @DisplayName("POST /api/concerts - Concert erstellen")
    class CreateConcertTests {
        
        private CreateConcertRequestDTO validRequest;
        private ConcertResponseDTO responseDTO;
        
        @BeforeEach
        void setUp() {
            validRequest = new CreateConcertRequestDTO(
                "Metallica Live 2025",
                FUTURE_DATE,
                "Mercedes-Benz Arena Berlin",
                "Exklusives Konzert"
            );
            
            responseDTO = ConcertResponseDTO.builder()
                .id("1")
                .name("Metallica Live 2025")
                .date(FUTURE_DATE)
                .venue("Mercedes-Benz Arena Berlin")
                .description("Exklusives Konzert")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        @Test
        @DisplayName("Sollte 201 Created zurückgeben mit gültiger ADMIN-Rolle")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201CreatedForValidAdminRequest() throws Exception {
            // Arrange
            when(concertApplicationService.createConcert(any(CreateConcertRequestDTO.class)))
                .thenReturn(responseDTO);
            
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
                    .with(csrf())
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Metallica Live 2025"))
            .andExpect(jsonPath("$.venue").value("Mercedes-Benz Arena Berlin"))
            .andExpect(jsonPath("$.id").value("1"));
            
            verify(concertApplicationService, times(1)).createConcert(any(CreateConcertRequestDTO.class));
        }
        
        @Test
        @DisplayName("Sollte 403 Forbidden zurückgeben ohne ADMIN-Rolle")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForbiddenForNonAdminUser() throws Exception {
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
                    .with(csrf())
            )
            .andExpect(status().isForbidden());
            
            verify(concertApplicationService, never()).createConcert(any());
        }
        
        @Test
        @DisplayName("Sollte 401 Unauthorized zurückgeben ohne Authentifizierung")
        void shouldReturn401UnauthorizedForUnauthenticatedRequest() throws Exception {
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
                    .with(csrf())
            )
            .andExpect(status().isUnauthorized());
            
            verify(concertApplicationService, never()).createConcert(any());
        }
        
        @Test
        @DisplayName("Sollte 400 Bad Request zurückgeben wenn Name null ist")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400BadRequestWhenNameIsNull() throws Exception {
            // Arrange
            CreateConcertRequestDTO invalidRequest = new CreateConcertRequestDTO(
                null,
                FUTURE_DATE,
                "Mercedes-Benz Arena Berlin",
                "Description"
            );
            
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
                    .with(csrf())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
            
            verify(concertApplicationService, never()).createConcert(any());
        }
        
        @Test
        @DisplayName("Sollte 400 Bad Request zurückgeben wenn Datum in Vergangenheit liegt")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400BadRequestWhenDateIsInPast() throws Exception {
            // Arrange
            CreateConcertRequestDTO invalidRequest = new CreateConcertRequestDTO(
                "Concert",
                LocalDateTime.now().minusMonths(1),
                "Venue",
                "Description"
            );
            
            when(concertApplicationService.createConcert(any()))
                .thenThrow(new InvalidConcertDateException("Datum in der Vergangenheit"));
            
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
                    .with(csrf())
            )
            .andExpect(status().isBadRequest());
        }
    }
    
    @Nested
    @DisplayName("PUT /api/concerts/{id} - Concert aktualisieren")
    class UpdateConcertTests {
        
        private UpdateConcertRequestDTO updateRequest;
        private ConcertResponseDTO responseDTO;
        
        @BeforeEach
        void setUp() {
            updateRequest = new UpdateConcertRequestDTO(
                "Metallica Updated",
                FUTURE_DATE.plusMonths(1),
                "O2 World Berlin",
                "Updated description"
            );
            
            responseDTO = ConcertResponseDTO.builder()
                .id("1")
                .name("Metallica Updated")
                .date(FUTURE_DATE.plusMonths(1))
                .venue("O2 World Berlin")
                .description("Updated description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
        
        @Test
        @DisplayName("Sollte 200 OK zurückgeben und Concert aktualisieren")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn200OkWhenUpdateSuccessful() throws Exception {
            // Arrange
            when(concertApplicationService.updateConcert(eq(1L), any(UpdateConcertRequestDTO.class)))
                .thenReturn(responseDTO);
            
            // Act & Assert
            mockMvc.perform(
                put("/api/concerts/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .with(csrf())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Metallica Updated"))
            .andExpect(jsonPath("$.venue").value("O2 World Berlin"));
            
            verify(concertApplicationService, times(1)).updateConcert(eq(1L), any());
        }
        
        @Test
        @DisplayName("Sollte 404 Not Found zurückgeben wenn Concert nicht existiert")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404NotFoundWhenConcertNotFound() throws Exception {
            // Arrange
            when(concertApplicationService.updateConcert(eq(999L), any(UpdateConcertRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Concert mit ID 999 nicht gefunden"));
            
            // Act & Assert
            mockMvc.perform(
                put("/api/concerts/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .with(csrf())
            )
            .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Sollte 403 Forbidden zurückgeben ohne ADMIN-Rolle")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForbiddenForNonAdminUser() throws Exception {
            // Act & Assert
            mockMvc.perform(
                put("/api/concerts/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .with(csrf())
            )
            .andExpect(status().isForbidden());
            
            verify(concertApplicationService, never()).updateConcert(any(Long.class), any());
        }
    }
    
    @Nested
    @DisplayName("DELETE /api/concerts/{id} - Concert löschen")
    class DeleteConcertTests {
        
        @Test
        @DisplayName("Sollte 204 No Content zurückgeben bei erfolgreicher Löschung")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn204NoContentWhenDeleteSuccessful() throws Exception {
            // Arrange
            doNothing().when(concertApplicationService).deleteConcert(1L);
            
            // Act & Assert
            mockMvc.perform(
                delete("/api/concerts/1")
                    .with(csrf())
            )
            .andExpect(status().isNoContent());
            
            verify(concertApplicationService, times(1)).deleteConcert(1L);
        }
        
        @Test
        @DisplayName("Sollte 404 Not Found zurückgeben wenn Concert nicht existiert")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404NotFoundWhenConcertNotFound() throws Exception {
            // Arrange
            doThrow(new IllegalArgumentException("Concert mit ID 999 nicht gefunden"))
                .when(concertApplicationService).deleteConcert(999L);
            
            // Act & Assert
            mockMvc.perform(
                delete("/api/concerts/999")
                    .with(csrf())
            )
            .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Sollte 403 Forbidden zurückgeben ohne ADMIN-Rolle")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForbiddenForNonAdminUser() throws Exception {
            // Act & Assert
            mockMvc.perform(
                delete("/api/concerts/1")
                    .with(csrf())
            )
            .andExpect(status().isForbidden());
            
            verify(concertApplicationService, never()).deleteConcert(any(Long.class));
        }
    }
    
    @Nested
    @DisplayName("POST /api/concerts/{id}/seats - Seats erstellen")
    class CreateSeatsTests {
        
        private CreateSeatsRequestDTO validRequest;
        
        @BeforeEach
        void setUp() {
            validRequest = new CreateSeatsRequestDTO(Arrays.asList(
                new CreateSeatRequestDTO("A-01", "VIP", "Block A", "A", "01", 89.99),
                new CreateSeatRequestDTO("A-02", "VIP", "Block A", "A", "02", 89.99),
                new CreateSeatRequestDTO("A-03", "VIP", "Block A", "A", "03", 89.99)
            ));
        }
        
        @Test
        @DisplayName("Sollte 201 Created zurückgeben bei erfolgreicher Seat-Erstellung")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn201CreatedWhenSeatsCreated() throws Exception {
            // Arrange
            doNothing().when(concertApplicationService).createSeats(eq(1L), anyList());
            
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts/1/seats")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
                    .with(csrf())
            )
            .andExpect(status().isCreated())
            .andExpect(content().string(containsString("Erfolgreich")));
            
            verify(concertApplicationService, times(1)).createSeats(eq(1L), anyList());
        }
        
        @Test
        @DisplayName("Sollte 400 Bad Request zurückgeben wenn keine Seats vorhanden sind")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400BadRequestWhenNoSeatsProvided() throws Exception {
            // Arrange
            CreateSeatsRequestDTO emptyRequest = new CreateSeatsRequestDTO(Arrays.asList());
            
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts/1/seats")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyRequest))
                    .with(csrf())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        
        @Test
        @DisplayName("Sollte 404 Not Found zurückgeben wenn Concert nicht existiert")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404NotFoundWhenConcertNotFound() throws Exception {
            // Arrange
            doThrow(new IllegalArgumentException("Concert mit ID 999 nicht gefunden"))
                .when(concertApplicationService).createSeats(eq(999L), anyList());
            
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts/999/seats")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
                    .with(csrf())
            )
            .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Sollte 403 Forbidden zurückgeben ohne ADMIN-Rolle")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForbiddenForNonAdminUser() throws Exception {
            // Act & Assert
            mockMvc.perform(
                post("/api/concerts/1/seats")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest))
                    .with(csrf())
            )
            .andExpect(status().isForbidden());
            
            verify(concertApplicationService, never()).createSeats(any(Long.class), anyList());
        }
    }
    
    @Nested
    @DisplayName("GET /api/concerts - Public Read Endpoints")
    class GetConcertsTests {
        
        @Test
        @DisplayName("Sollte 200 OK zurückgeben ohne Authentifizierung (public endpoint)")
        void shouldReturn200OkWithoutAuthentication() throws Exception {
            // Arrange
            when(concertApplicationService.getAllConcerts()).thenReturn(Arrays.asList());
            
            // Act & Assert
            mockMvc.perform(get("/api/concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
        
        @Test
        @DisplayName("Sollte 200 OK mit Concert-Liste zurückgeben")
        void shouldReturn200OkWithConcertList() throws Exception {
            // Arrange
            ConcertResponseDTO concert = ConcertResponseDTO.builder()
                .id("1")
                .name("Test Concert")
                .date(FUTURE_DATE)
                .venue("Test Venue")
                .build();
            
            when(concertApplicationService.getAllConcerts()).thenReturn(Arrays.asList(concert));
            
            // Act & Assert
            mockMvc.perform(get("/api/concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Concert"));
        }
    }
}

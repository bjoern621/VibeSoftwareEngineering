package com.concertcomparison.presentation.controller;

import com.concertcomparison.domain.model.User;
import com.concertcomparison.domain.model.UserRole;
import com.concertcomparison.domain.repository.UserRepository;
import com.concertcomparison.presentation.dto.LoginRequest;
import com.concertcomparison.presentation.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für Auth-Flow.
 * 
 * Testet den kompletten Auth-Flow:
 * - User-Registrierung
 * - Login mit JWT Token
 * - Authentifizierter Zugriff auf geschützte Endpoints
 * - Ablehnung von invaliden Tokens
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Auth Integration Tests")
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Test User erstellen
        testUser = User.create(
                "existing@example.com",
                passwordEncoder.encode("password123"),
                "Max",
                "Mustermann",
                UserRole.USER
        );
        userRepository.save(testUser);
    }
    
    // ==================== REGISTRATION TESTS ====================
    
    @Test
    @DisplayName("POST /api/auth/register - Erfolgreiche Registrierung")
    void register_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "John",
                "Doe"
        );
        
        // When / Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }
    
    @Test
    @DisplayName("POST /api/auth/register - Fehlschlag bei Duplicate Email")
    void register_DuplicateEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                "John",
                "Doe"
        );
        
        // When / Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/register - Validation Error bei ungültiger Email")
    void register_InvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "password123",
                "John",
                "Doe"
        );
        
        // When / Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/register - Validation Error bei zu kurzem Password")
    void register_PasswordTooShort() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "short",
                "John",
                "Doe"
        );
        
        // When / Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    // ==================== LOGIN TESTS ====================
    
    @Test
    @DisplayName("POST /api/auth/login - Erfolgreicher Login")
    void login_Success() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("existing@example.com", "password123");
        
        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("existing@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
    
    @Test
    @DisplayName("POST /api/auth/login - Fehlschlag bei falschem Password")
    void login_WrongPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("existing@example.com", "wrongPassword");
        
        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /api/auth/login - Fehlschlag bei unbekannter Email")
    void login_UnknownEmail() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        
        // When / Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    // ==================== PROFILE TESTS ====================
    
    @Test
    @DisplayName("GET /api/users/profile - Erfolgreiche Profil-Abfrage mit JWT")
    void getProfile_WithValidToken() throws Exception {
        // Given - Login durchführen und Token erhalten
        LoginRequest loginRequest = new LoginRequest("existing@example.com", "password123");
        
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String token = objectMapper.readTree(loginResponse).get("token").asText();
        
        // When / Then - Profil mit Token abrufen
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("existing@example.com"))
                .andExpect(jsonPath("$.firstName").value("Max"))
                .andExpect(jsonPath("$.lastName").value("Mustermann"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
    
    @Test
    @DisplayName("GET /api/users/profile - 401 ohne Token")
    void getProfile_WithoutToken() throws Exception {
        // When / Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("GET /api/users/profile - 401 mit invalidem Token")
    void getProfile_WithInvalidToken() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When / Then
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("GET /api/users/profile - 401 mit abgelaufenem Token")
    void getProfile_WithExpiredToken() throws Exception {
        // Given - Abgelaufener Token (simuliert)
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.invalid";
        
        // When / Then
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}

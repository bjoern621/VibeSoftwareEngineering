package com.concertcomparison.presentation.controller;

import com.concertcomparison.application.service.AuthService;
import com.concertcomparison.presentation.dto.LoginRequest;
import com.concertcomparison.presentation.dto.LoginResponse;
import com.concertcomparison.presentation.dto.RegisterRequest;
import com.concertcomparison.presentation.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Authentifizierung und User-Management.
 * 
 * Endpoints:
 * - POST /api/auth/register - User-Registrierung
 * - POST /api/auth/login - User-Login (JWT Token)
 * - GET /api/users/profile - User-Profil abfragen
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "User-Registrierung, Login und Profil-Management")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Registriert einen neuen User.
     * 
     * @param request RegisterRequest
     * @return UserProfileResponse (201 Created)
     */
    @PostMapping("/auth/register")
    @Operation(summary = "User registrieren", 
               description = "Erstellt einen neuen User Account mit BCrypt Password Hashing")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserProfileResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Authentifiziert einen User und liefert JWT Token.
     * 
     * @param request LoginRequest
     * @return LoginResponse mit JWT Token (200 OK)
     */
    @PostMapping("/auth/login")
    @Operation(summary = "User login", 
               description = "Authentifiziert User und liefert JWT Token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Liefert das Profil des aktuell authentifizierten Users.
     * 
     * @param authentication Spring Security Authentication
     * @return UserProfileResponse (200 OK)
     */
    @GetMapping("/users/profile")
    @Operation(summary = "User-Profil abrufen", 
               description = "Liefert Profil-Daten des aktuell eingeloggten Users")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse response = authService.getProfile(email);
        return ResponseEntity.ok(response);
    }
}

package com.rentacar.presentation.controller;

import com.rentacar.application.service.CustomerApplicationService;
import com.rentacar.domain.service.RefreshTokenService;
import com.rentacar.presentation.dto.AuthenticationResponseDTO;
import com.rentacar.presentation.dto.RefreshTokenRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Authentifizierungs-Operationen.
 * Stellt Endpoints für Token-Refresh bereit.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerApplicationService customerApplicationService;

    public AuthController(CustomerApplicationService customerApplicationService) {
        this.customerApplicationService = customerApplicationService;
    }

    /**
     * Erneuert einen Access-Token mittels Refresh-Token.
     * Öffentlicher Endpoint (keine Authentifizierung erforderlich).
     * 
     * Der alte Refresh-Token wird invalidiert und ein neuer wird generiert.
     *
     * @param request Refresh-Token-Anfrage
     * @return Neuer Access-Token und neuer Refresh-Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            AuthenticationResponseDTO response = 
                customerApplicationService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (RefreshTokenService.RefreshTokenException e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Ungültiger oder abgelaufener Refresh-Token: " + e.getMessage());
        }
    }
}


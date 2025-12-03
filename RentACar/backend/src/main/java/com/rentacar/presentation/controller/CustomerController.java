package com.rentacar.presentation.controller;

import com.rentacar.application.service.CustomerApplicationService;
import com.rentacar.presentation.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Kundenverwaltung.
 * Stellt Endpoints für Registrierung, Login, Profilverwaltung und E-Mail-Verifikation bereit.
 */
@RestController
@RequestMapping("/api/kunden")
public class CustomerController {

    private final CustomerApplicationService customerApplicationService;

    public CustomerController(CustomerApplicationService customerApplicationService) {
        this.customerApplicationService = customerApplicationService;
    }

    /**
     * Registriert einen neuen Kunden.
     * Öffentlicher Endpoint (keine Authentifizierung erforderlich).
     *
     * @param request Registrierungsdaten
     * @return JWT-Token und Kundendaten
     */
    @PostMapping("/registrierung")
    public ResponseEntity<AuthenticationResponseDTO> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequestDTO request) {
        AuthenticationResponseDTO response = customerApplicationService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authentifiziert einen Kunden (Login).
     * Öffentlicher Endpoint (keine Authentifizierung erforderlich).
     *
     * @param request Login-Daten
     * @return JWT-Token und Kundendaten
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        AuthenticationResponseDTO response = customerApplicationService.authenticateCustomer(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lädt das Profil des aktuell authentifizierten Kunden.
     * Erfordert Authentifizierung (JWT-Token).
     *
     * @param userDetails Authentifizierter Benutzer (automatisch injiziert)
     * @return Kundenprofil
     */
    @GetMapping("/profil")
    public ResponseEntity<CustomerProfileResponseDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        CustomerProfileResponseDTO profile =
                customerApplicationService.getCustomerProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    /**
     * Aktualisiert das Profil des aktuell authentifizierten Kunden.
     * Erfordert Authentifizierung (JWT-Token).
     * Kunde kann nur eigene Daten ändern.
     *
     * @param userDetails Authentifizierter Benutzer (automatisch injiziert)
     * @param request Aktualisierungsdaten
     * @return Aktualisiertes Kundenprofil
     */
    @PutMapping("/profil")
    public ResponseEntity<CustomerProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateCustomerProfileRequestDTO request) {
        CustomerProfileResponseDTO profile =
                customerApplicationService.updateCustomerProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Verifiziert die E-Mail-Adresse eines Kunden.
     * Öffentlicher Endpoint (keine Authentifizierung erforderlich).
     *
     * @param token Verifikations-Token (als Query-Parameter)
     * @return Erfolgsmeldung
     */
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        customerApplicationService.verifyEmail(token);
        return ResponseEntity.ok("E-Mail-Adresse erfolgreich verifiziert");
    }

    /**
     * Loggt einen Kunden aus und invalidiert den JWT-Token.
     * Erfordert Authentifizierung (JWT-Token).
     * Der Token wird auf die Blacklist gesetzt und ist ab sofort ungültig.
     *
     * @param authHeader Authorization-Header mit JWT-Token
     * @return Erfolgsmeldung
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Ungültiger Authorization-Header");
        }

        String token = authHeader.substring(7);
        customerApplicationService.logoutCustomer(token);

        return ResponseEntity.ok("Erfolgreich ausgeloggt");
    }
}

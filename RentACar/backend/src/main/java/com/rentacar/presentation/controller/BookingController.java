package com.rentacar.presentation.controller;

import com.rentacar.application.service.BookingApplicationService;
import com.rentacar.presentation.dto.PriceCalculationRequestDTO;
import com.rentacar.presentation.dto.PriceCalculationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Buchungsfunktionalität.
 * 
 * Stellt Endpoints für Buchungsverwaltung und Preisberechnungen bereit.
 */
@RestController
@RequestMapping("/api/buchungen")
public class BookingController {
    
    private final BookingApplicationService bookingApplicationService;
    
    public BookingController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }
    
    /**
     * Berechnet den Preis für eine geplante Miete ohne eine tatsächliche Buchung zu erstellen.
     * 
     * Dieser Endpoint ermöglicht es Kunden, vorab den Preis für verschiedene
     * Konfigurationen zu berechnen, bevor sie eine Buchung aufgeben.
     * 
     * @param request Request mit Fahrzeugtyp, Mietdauer und Zusatzleistungen
     * @return Detaillierte Preisaufschlüsselung
     */
    @PostMapping("/preis-berechnen")
    public ResponseEntity<PriceCalculationResponseDTO> calculatePrice(
        @Valid @RequestBody PriceCalculationRequestDTO request
    ) {
        PriceCalculationResponseDTO response = bookingApplicationService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Exception Handler für IllegalArgumentException.
     * 
     * Behandelt Validierungsfehler aus der Business-Logik.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Einfaches Error Response DTO.
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        
        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
        
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}

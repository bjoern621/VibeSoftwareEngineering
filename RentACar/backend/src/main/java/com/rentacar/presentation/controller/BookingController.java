package com.rentacar.presentation.controller;

import com.rentacar.application.service.BookingApplicationService;
import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.BookingStatus;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.presentation.dto.BookingHistoryDto;
import com.rentacar.presentation.dto.FahrzeugInfoDto;
import com.rentacar.presentation.dto.FilialeInfoDto;
import com.rentacar.presentation.dto.PriceCalculationRequestDTO;
import com.rentacar.presentation.dto.PriceCalculationResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller für Buchungsfunktionalität.
 * 
 * Stellt Endpoints für Buchungsverwaltung und Preisberechnungen bereit.
 */
@RestController
@RequestMapping("/api")
public class BookingController {
    
    private final BookingApplicationService bookingApplicationService;
    private final JwtUtil jwtUtil;
    
    public BookingController(BookingApplicationService bookingApplicationService,
                            JwtUtil jwtUtil) {
        this.bookingApplicationService = bookingApplicationService;
        this.jwtUtil = jwtUtil;
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
    @PostMapping("/buchungen/preis-berechnen")
    public ResponseEntity<PriceCalculationResponseDTO> calculatePrice(
        @Valid @RequestBody PriceCalculationRequestDTO request
    ) {
        PriceCalculationResponseDTO response = bookingApplicationService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/kunden/meine-buchungen
     * Aktueller Kunde sieht seine eigenen Buchungen.
     * 
     * @param authentication Spring Security Authentication
     * @param status Optional: Filter nach Buchungsstatus
     * @return Liste der Buchungen des aktuellen Kunden
     */
    @GetMapping("/kunden/meine-buchungen")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingHistoryDto>> getMyBookings(
            Authentication authentication,
            @RequestParam(required = false) BookingStatus status) {
        
        Long customerId = jwtUtil.extractCustomerId(authentication);
        List<Booking> bookings = bookingApplicationService.getCustomerBookingsByStatus(customerId, status);
        
        List<BookingHistoryDto> response = bookings.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/kunden/{id}/buchungen
     * Mitarbeiter/Admins können Buchungshistorie aller Kunden sehen.
     * 
     * @param id Kunden-ID
     * @param status Optional: Filter nach Buchungsstatus
     * @return Liste der Buchungen des angegebenen Kunden
     */
    @GetMapping("/kunden/{id}/buchungen")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<BookingHistoryDto>> getCustomerBookings(
            @PathVariable Long id,
            @RequestParam(required = false) BookingStatus status) {
        
        List<Booking> bookings = bookingApplicationService.getCustomerBookingsByStatus(id, status);
        
        List<BookingHistoryDto> response = bookings.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mapped Booking Entity zu DTO (Ubiquitous Language: Deutsche Felder).
     * 
     * @param booking Booking Entity
     * @return BookingHistoryDto
     */
    private BookingHistoryDto mapToDto(Booking booking) {
        return new BookingHistoryDto(
            booking.getId(),
            new FahrzeugInfoDto(
                booking.getVehicle().getId(),
                booking.getVehicle().getLicensePlate().getValue(),
                booking.getVehicle().getBrand(),
                booking.getVehicle().getModel(),
                booking.getVehicle().getVehicleType().getDisplayName()
            ),
            new FilialeInfoDto(
                booking.getPickupBranch().getId(),
                booking.getPickupBranch().getName(),
                booking.getPickupBranch().getAddress()
            ),
            new FilialeInfoDto(
                booking.getReturnBranch().getId(),
                booking.getReturnBranch().getName(),
                booking.getReturnBranch().getAddress()
            ),
            booking.getPickupDateTime(),
            booking.getReturnDateTime(),
            booking.getStatus(),
            booking.getTotalPrice(),
            booking.getCurrency(),
            booking.getAdditionalServices(),
            booking.getCreatedAt(),
            booking.getCancellationReason()
        );
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

package com.rentacar.presentation.controller;

import com.rentacar.application.command.CancelBookingCommand;
import com.rentacar.application.service.BookingApplicationService;
import com.rentacar.domain.exception.BookingNotFoundException;
import com.rentacar.domain.exception.CancellationDeadlineExceededException;
import com.rentacar.domain.exception.UnauthorizedBookingAccessException;
import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.BookingStatus;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.infrastructure.security.RoleConstants;
import com.rentacar.presentation.dto.BookingHistoryDto;
import com.rentacar.presentation.dto.CreateBookingRequestDTO;
import com.rentacar.presentation.dto.CancelBookingRequestDto;
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
    @PreAuthorize(RoleConstants.CUSTOMER)
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
    @PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)
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
     * GET /api/buchungen
     * Mitarbeiter/Admins können alle Buchungen sehen.
     * 
     * @param status Optional: Filter nach Buchungsstatus
     * @return Liste aller Buchungen
     */
    @GetMapping("/buchungen")
    @PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)
    public ResponseEntity<List<BookingHistoryDto>> getAllBookings(
            @RequestParam(required = false) BookingStatus status) {
        
        List<Booking> bookings = bookingApplicationService.getAllBookings(status);
        
        List<BookingHistoryDto> response = bookings.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Erstellt eine neue Buchung.
     *
     * @param request Buchungsanfrage
     * @param authentication Authentifizierung
     * @return Die erstellte Buchung (als DTO)
     */
    @PostMapping("/buchungen")
    @PreAuthorize(RoleConstants.CUSTOMER)
    public ResponseEntity<BookingHistoryDto> createBooking(
            @Valid @RequestBody CreateBookingRequestDTO request,
            Authentication authentication) {

        Long customerId = jwtUtil.extractCustomerId(authentication);
        Booking booking = bookingApplicationService.createBooking(customerId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(booking));
    }

    /**
     * GET /api/buchungen/{id}/zusatzkosten
     * Ruft die Zusatzkosten für eine Buchung ab.
     *
     * @param id Buchungs-ID
     * @return Aufschlüsselung der Zusatzkosten
     */
    @GetMapping("/buchungen/{id}/zusatzkosten")
    @PreAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public ResponseEntity<com.rentacar.presentation.dto.AdditionalCostsDTO> getAdditionalCosts(@PathVariable Long id) {
        com.rentacar.presentation.dto.AdditionalCostsDTO costs = bookingApplicationService.getAdditionalCosts(id);
        return ResponseEntity.ok(costs);
    }
    
    /**
     * POST /api/buchungen/{id}/bestaetigen
     * Bestätigt eine Buchung (REQUESTED → CONFIRMED).
     * Nur für Mitarbeiter/Admins.
     *
     * @param id Buchungs-ID
     * @return Die bestätigte Buchung
     */
    @PostMapping("/buchungen/{id}/bestaetigen")
    @PreAuthorize(RoleConstants.EMPLOYEE_OR_ADMIN)
    public ResponseEntity<BookingHistoryDto> confirmBooking(@PathVariable Long id) {
        Booking booking = bookingApplicationService.confirmBooking(id);
        return ResponseEntity.ok(mapToDto(booking));
    }

    /**
     * GET /api/buchungen/{id}
     * Ruft eine einzelne Buchung anhand ihrer ID ab.
     * 
     * @param id Buchungs-ID
     * @param authentication Spring Security Authentication
     * @return Buchungsdetails als DTO
     */
    @GetMapping("/buchungen/{id}")
    @PreAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public ResponseEntity<BookingHistoryDto> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {

        Booking booking = bookingApplicationService.getBookingById(id);

        // Kunden dürfen nur ihre eigenen Buchungen sehen
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            Long customerId = jwtUtil.extractCustomerId(authentication);
            if (!booking.getCustomer().getId().equals(customerId)) {
                throw new UnauthorizedBookingAccessException(id, customerId);
            }
        }

        return ResponseEntity.ok(mapToDto(booking));
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
    
    // ========== Buchungsstornierung Endpoint ==========

    /**
     * DELETE /api/buchungen/{id}/stornieren
     *
     * Storniert eine Buchung (mind. 24h vor Abholung).
     *
     * Authorization:
     * - CUSTOMER: Nur eigene Buchungen
     * - EMPLOYEE/ADMIN: Alle Buchungen (via @PreAuthorize)
     *
     * @param id Buchungs-ID
     * @param request Optionaler Stornierungsgrund
     * @param authentication JWT-Token
     * @return 204 No Content bei Erfolg
     * @throws BookingNotFoundException wenn Buchung nicht existiert (404)
     * @throws UnauthorizedBookingAccessException wenn keine Berechtigung (403)
     * @throws CancellationDeadlineExceededException wenn < 24h vor Abholung (400)
     */
    @DeleteMapping("/buchungen/{id}/stornieren")
    @PreAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public ResponseEntity<Void> cancelBooking(
        @PathVariable Long id,
        @RequestBody(required = false) @Valid CancelBookingRequestDto request,
        Authentication authentication
    ) {
        // Customer-ID aus JWT extrahieren (null für Employee/Admin)
        Long customerId = null;
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            customerId = jwtUtil.extractCustomerId(authentication);
        }

        String reason = (request != null) ? request.reason() : null;

        // Application Service aufrufen (Use-Case-Orchestrierung)
        CancelBookingCommand command = new CancelBookingCommand(id, customerId, reason);
        bookingApplicationService.cancelBooking(command);

        // 204 No Content zurückgeben
        return ResponseEntity.noContent().build();
    }

    // ========== Exception Handlers ==========

    /**
     * Exception Handler für BookingNotFoundException.
     *
     * @param ex BookingNotFoundException
     * @return 404 Not Found
     */
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(BookingNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Exception Handler für UnauthorizedBookingAccessException.
     *
     * @param ex UnauthorizedBookingAccessException
     * @return 403 Forbidden
     */
    @ExceptionHandler(UnauthorizedBookingAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedBookingAccess(
        UnauthorizedBookingAccessException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Exception Handler für CancellationDeadlineExceededException.
     *
     * @param ex CancellationDeadlineExceededException
     * @return 400 Bad Request
     */
    @ExceptionHandler(CancellationDeadlineExceededException.class)
    public ResponseEntity<ErrorResponse> handleCancellationDeadlineExceeded(
        CancellationDeadlineExceededException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Exception Handler für BookingStatusTransitionException.
     *
     * @param ex BookingStatusTransitionException
     * @return 400 Bad Request
     */
    @ExceptionHandler(com.rentacar.domain.exception.BookingStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleBookingStatusTransition(
        com.rentacar.domain.exception.BookingStatusTransitionException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
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

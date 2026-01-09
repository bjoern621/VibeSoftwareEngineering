package com.rentacar.infrastructure.event;

import com.rentacar.domain.event.BookingCancelled;
import com.rentacar.domain.exception.VehicleNotFoundException;
import com.rentacar.domain.model.Vehicle;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.domain.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event Handler für Buchungs-Events.
 * 
 * Reagiert ASYNCHRON auf Domain Events (lose Kopplung zwischen Aggregaten).
 * 
 * DDD-Pattern: Event-Driven Architecture ermöglicht:
 * - Lose Kopplung zwischen Booking und Vehicle Aggregaten
 * - Asynchrone Integration (E-Mail blockiert nicht die Stornierung)
 * - Erweiterbarkeit (neue Event-Handler ohne Code-Änderung am Use-Case)
 */
@Component
public class BookingEventHandler {

    private static final Logger log = LoggerFactory.getLogger(BookingEventHandler.class);

    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;

    public BookingEventHandler(
        VehicleRepository vehicleRepository,
        EmailService emailService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.emailService = emailService;
    }

    /**
     * Reagiert auf BookingCancelled Event:
     * 1. Vehicle wieder verfügbar machen
     * 2. E-Mail an Kunden versenden
     * 
     * @param event BookingCancelled Event
     */
    @EventListener
    @Async // Asynchrone Verarbeitung (nicht-blockierend)
    @Transactional
    public void handleBookingCancelled(BookingCancelled event) {
        log.info("BookingCancelled Event empfangen: BookingId={}, VehicleId={}, CustomerId={}",
            event.getBookingId(), event.getVehicleId(), event.getCustomerId());

        try {
            // 1. Vehicle Aggregate laden und verfügbar machen
            Vehicle vehicle = vehicleRepository.findById(event.getVehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(event.getVehicleId()));
            
            vehicle.makeAvailable();
            vehicleRepository.save(vehicle);
            log.info("Fahrzeug {} (ID: {}) wieder verfügbar gemacht",
                vehicle.getLicensePlate(), event.getVehicleId());

            // 2. E-Mail an Kunden versenden
            emailService.sendBookingCancellationEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getBookingId(),
                event.getCancellationReason()
            );
            log.info("Stornierungsbestätigung an {} versendet", event.getCustomerEmail());

        } catch (Exception e) {
            log.error("Fehler bei Verarbeitung von BookingCancelled Event für Buchung {}: {}",
                event.getBookingId(), e.getMessage(), e);
            // In Produktion: Event in Dead-Letter-Queue schreiben oder Retry-Mechanismus
            throw e;
        }
    }
}


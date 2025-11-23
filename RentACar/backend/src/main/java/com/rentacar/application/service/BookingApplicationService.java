package com.rentacar.application.service;

import com.rentacar.application.command.CancelBookingCommand;
import com.rentacar.domain.event.BookingCancelled;
import com.rentacar.domain.exception.BookingNotFoundException;
import com.rentacar.domain.exception.CustomerNotFoundException;
import com.rentacar.domain.model.*;
import com.rentacar.domain.exception.UnauthorizedBookingAccessException;
import com.rentacar.domain.model.AdditionalServiceType;
import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.BookingStatus;
import com.rentacar.domain.model.DateRange;
import com.rentacar.domain.model.PricingCalculation;
import com.rentacar.domain.model.VehicleType;
import com.rentacar.domain.repository.BookingRepository;
import com.rentacar.domain.repository.BranchRepository;
import com.rentacar.domain.repository.CustomerRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.domain.service.BookingDomainService;
import com.rentacar.domain.service.PricingService;
import com.rentacar.presentation.dto.CreateBookingRequestDTO;
import com.rentacar.presentation.dto.PriceCalculationRequestDTO;
import com.rentacar.presentation.dto.PriceCalculationResponseDTO;
import com.rentacar.presentation.dto.PriceCalculationResponseDTO.AdditionalServiceItemDTO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application Service für Buchungsfunktionalität.
 * 
 * Orchestriert Use Cases rund um Buchungen, inklusive Preisberechnung und Buchungshistorie.
 */
@Service
@Transactional
public class BookingApplicationService {
    
    private final PricingService pricingService;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final BookingDomainService bookingDomainService;
    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final com.rentacar.domain.repository.RentalAgreementRepository rentalAgreementRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BookingApplicationService(BookingRepository bookingRepository,
                                     CustomerRepository customerRepository,
                                     BookingDomainService bookingDomainService,
                                     VehicleRepository vehicleRepository,
                                     BranchRepository branchRepository,
                                     com.rentacar.domain.repository.RentalAgreementRepository rentalAgreementRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.pricingService = new PricingService();
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.bookingDomainService = bookingDomainService;
        this.vehicleRepository = vehicleRepository;
        this.branchRepository = branchRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.eventPublisher = eventPublisher;

    }

    /**
     * Erstellt eine neue Buchung.
     *
     * @param customerId ID des Kunden
     * @param request Buchungsanfrage
     * @return Die erstellte Buchung
     */
    public Booking createBooking(Long customerId, CreateBookingRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new IllegalArgumentException("Fahrzeug nicht gefunden: " + request.getVehicleId()));

        Branch pickupBranch = branchRepository.findById(request.getPickupBranchId())
            .orElseThrow(() -> new IllegalArgumentException("Abholfiliale nicht gefunden: " + request.getPickupBranchId()));

        Branch returnBranch = branchRepository.findById(request.getReturnBranchId())
            .orElseThrow(() -> new IllegalArgumentException("Rückgabefiliale nicht gefunden: " + request.getReturnBranchId()));

        Set<AdditionalServiceType> additionalServices = new HashSet<>(parseAdditionalServices(request.getAdditionalServices()));

        return bookingDomainService.createBooking(
            customer,
            vehicle,
            pickupBranch,
            returnBranch,
            request.getPickupDateTime(),
            request.getReturnDateTime(),
            additionalServices
        );
    }
    
    /**
     * Berechnet den Preis für eine geplante Miete.
     * 
     * @param request Request mit Fahrzeugtyp, Zeitraum und Zusatzleistungen
     * @return Detaillierte Preisberechnung
     * @throws IllegalArgumentException wenn Parameter ungültig sind
     */
    public PriceCalculationResponseDTO calculatePrice(PriceCalculationRequestDTO request) {
        // Validierung und Mapping: String → Domain Objects
        VehicleType vehicleType = parseVehicleType(request.getVehicleType());
        DateRange rentalPeriod = new DateRange(
            request.getPickupDateTime(),
            request.getReturnDateTime()
        );
        List<AdditionalServiceType> additionalServices = parseAdditionalServices(
            request.getAdditionalServices()
        );
        
        // Domain Service aufrufen
        PricingCalculation calculation = pricingService.calculatePrice(
            vehicleType,
            rentalPeriod,
            additionalServices
        );
        
        // Domain Object → Response DTO mappen
        return mapToResponseDTO(calculation);
    }
    
    /**
     * Parst den Fahrzeugtyp aus dem Request String.
     * 
     * @param vehicleTypeString Name des Fahrzeugtyps (z.B. "COMPACT_CAR")
     * @return VehicleType Enum
     * @throws IllegalArgumentException wenn der Fahrzeugtyp ungültig ist
     */
    private VehicleType parseVehicleType(String vehicleTypeString) {
        return VehicleType.fromString(vehicleTypeString)
            .orElseThrow(() -> new IllegalArgumentException(
                "Ungültiger Fahrzeugtyp: " + vehicleTypeString + 
                ". Erlaubte Werte: " + String.join(", ", getVehicleTypeNames())
            ));
    }
    
    /**
     * Parst die Liste der Zusatzleistungen aus den Request Strings.
     * 
     * @param serviceStrings Liste von Zusatzleistungs-Namen
     * @return Liste von AdditionalServiceType Enums
     * @throws IllegalArgumentException wenn eine Zusatzleistung ungültig ist
     */
    private List<AdditionalServiceType> parseAdditionalServices(List<String> serviceStrings) {
        if (serviceStrings == null || serviceStrings.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<AdditionalServiceType> services = new ArrayList<>();
        for (String serviceString : serviceStrings) {
            AdditionalServiceType service = AdditionalServiceType.fromString(serviceString)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Ungültige Zusatzleistung: " + serviceString + 
                    ". Erlaubte Werte: " + String.join(", ", getAdditionalServiceNames())
                ));
            services.add(service);
        }
        
        return services;
    }
    
    /**
     * Mappt die Domain-Preisberechnung zu einem Response DTO.
     * 
     * @param calculation Domain-Preisberechnung
     * @return Response DTO
     */
    private PriceCalculationResponseDTO mapToResponseDTO(PricingCalculation calculation) {
        List<AdditionalServiceItemDTO> serviceItemDTOs = calculation.getAdditionalServices()
            .stream()
            .map(item -> new AdditionalServiceItemDTO(
                item.getServiceType().name(),
                item.getServiceType().getDisplayName(),
                item.getServiceType().getDailyPrice(),
                item.getPrice()
            ))
            .collect(Collectors.toList());
        
        return new PriceCalculationResponseDTO(
            calculation.getVehicleType().name(),
            calculation.getVehicleType().getDisplayName(),
            calculation.getNumberOfDays(),
            calculation.getVehicleType().getDailyBaseRate(),
            calculation.getBasePrice(),
            serviceItemDTOs,
            calculation.getAdditionalServicesPrice(),
            calculation.getTotalPrice()
        );
    }
    
    /**
     * @return Liste aller verfügbaren Fahrzeugtyp-Namen
     */
    private List<String> getVehicleTypeNames() {
        return VehicleType.getAllTypes()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toList());
    }
    
    /**
     * @return Liste aller verfügbaren Zusatzleistungs-Namen
     */
    private List<String> getAdditionalServiceNames() {
        return AdditionalServiceType.getAllServices()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toList());
    }
    
    // ========== Buchungshistorie Use Cases ==========
    
    /**
     * Ruft alle Buchungen eines Kunden ab (chronologisch, neueste zuerst).
     * 
     * @param customerId ID des Kunden
     * @return Liste aller Buchungen
     * @throws CustomerNotFoundException wenn Kunde nicht existiert
     */
    @Transactional(readOnly = true)
    public List<Booking> getCustomerBookings(Long customerId) {
        validateCustomerExists(customerId);
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Ruft Buchungen eines Kunden gefiltert nach Status ab.
     * 
     * @param customerId ID des Kunden
     * @param status Gewünschter Status (null = alle)
     * @return Liste der Buchungen
     * @throws CustomerNotFoundException wenn Kunde nicht existiert
     */
    @Transactional(readOnly = true)
    public List<Booking> getCustomerBookingsByStatus(Long customerId, BookingStatus status) {
        validateCustomerExists(customerId);
        
        if (status == null) {
            return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        }
        
        return bookingRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
    }

    /**
     * Ruft eine spezifische Buchung ab.
     * 
     * @param bookingId ID der Buchung
     * @return Buchung
     * @throws BookingNotFoundException wenn Buchung nicht gefunden
     */
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    /**
     * Validiert, ob ein Kunde existiert.
     * 
     * @param customerId ID des Kunden
     * @throws CustomerNotFoundException wenn Kunde nicht existiert
     */
    private void validateCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
    }

    public com.rentacar.presentation.dto.AdditionalCostsDTO getAdditionalCosts(Long bookingId) {
        RentalAgreement agreement = rentalAgreementRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Rental agreement not found for booking ID: " + bookingId));

        AdditionalCosts costs = agreement.getAdditionalCosts();
        if (costs == null) {
             return new com.rentacar.presentation.dto.AdditionalCostsDTO(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO);
        }

        return new com.rentacar.presentation.dto.AdditionalCostsDTO(
                costs.getLateFee(),
                costs.getExcessMileageFee(),
                costs.getDamageCost(),
                costs.getTotalAdditionalCost()
        );
    }

    // ========== Buchungsstornierung Use Case ==========

    /**
     * Use-Case: Buchung stornieren.
     *
     * Orchestriert:
     * 1. Booking laden
     * 2. Authorization (Business Rule: nur Owner darf stornieren)
     * 3. Domain-Logik ausführen (booking.cancel() mit 24h-Validierung)
     * 4. Persistierung
     * 5. Domain Event publizieren (Vehicle-Verfügbarkeit + E-Mail asynchron via Event Handler)
     *
     * @param command Stornierungskommando mit bookingId, customerId, reason
     * @throws BookingNotFoundException wenn Buchung nicht existiert
     * @throws UnauthorizedBookingAccessException wenn Kunde nicht Owner ist
     * @throws CancellationDeadlineExceededException wenn < 24h vor Abholung (aus booking.cancel())
     */
    public void cancelBooking(CancelBookingCommand command) {
        // 1. Booking Aggregate laden
        Booking booking = bookingRepository.findById(command.bookingId())
            .orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

        // 2. Authorization (Business Rule im Application Layer)
        validateCancellationAuthorization(booking, command.customerId());

        // 3. Domain-Logik ausführen (24h-Validierung + Status-Transition)
        booking.cancel(LocalDateTime.now(), command.reason());
        bookingRepository.save(booking);

        // 4. Domain Event publizieren (ASYNCHRONE Integration!)
        // Event-Handler wird Vehicle verfügbar machen + E-Mail versenden
        BookingCancelled event = new BookingCancelled(
            booking.getId(),
            booking.getCustomer().getId(),
            booking.getVehicle().getId(),
            booking.getCustomer().getEmail(),
            booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName(),
            command.reason(),
            LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * Validiert, ob der Kunde berechtigt ist, die Buchung zu stornieren.
     *
     * Business Rule: Nur der Buchungseigentümer darf stornieren.
     * (Employee/Admin-Check erfolgt im Controller via @PreAuthorize)
     *
     * @param booking die zu stornierende Buchung
     * @param customerId ID des stornierenden Kunden (null für Employee/Admin)
     * @throws UnauthorizedBookingAccessException wenn Kunde nicht Owner ist
     */
    private void validateCancellationAuthorization(Booking booking, Long customerId) {
        // Employee/Admin haben keine customerId (null) und dürfen alles stornieren
        if (customerId == null) {
            return;
        }

        // Für Customers: nur eigene Buchungen
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedBookingAccessException(booking.getId(), customerId);
        }
    }
}

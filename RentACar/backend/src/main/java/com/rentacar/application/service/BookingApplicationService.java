package com.rentacar.application.service;

import com.rentacar.domain.model.AdditionalServiceType;
import com.rentacar.domain.model.DateRange;
import com.rentacar.domain.model.PricingCalculation;
import com.rentacar.domain.model.VehicleType;
import com.rentacar.domain.service.PricingService;
import com.rentacar.presentation.dto.PriceCalculationRequestDTO;
import com.rentacar.presentation.dto.PriceCalculationResponseDTO;
import com.rentacar.presentation.dto.PriceCalculationResponseDTO.AdditionalServiceItemDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service für Buchungsfunktionalität.
 * 
 * Orchestriert Use Cases rund um Buchungen, inklusive Preisberechnung.
 */
@Service
public class BookingApplicationService {
    
    private final PricingService pricingService;
    
    public BookingApplicationService() {
        this.pricingService = new PricingService();
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
}

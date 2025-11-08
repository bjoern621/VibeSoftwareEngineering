package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.*;
import com.travelreimburse.domain.exception.DestinationNotFoundException;
import com.travelreimburse.domain.exception.InvalidCountryCodeException;
import com.travelreimburse.domain.model.*;
import com.travelreimburse.domain.repository.TravelDestinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service für Reiseziele
 * Orchestriert Use Cases für Visa- und Impfanforderungen
 * 
 * DDD: Service ist Orchestrator - ruft Entity-Methoden auf, 
 *      implementiert NICHT selbst Geschäftslogik
 */
@Service
@Transactional(readOnly = true)
public class TravelDestinationService {

    private final TravelDestinationRepository repository;

    public TravelDestinationService(TravelDestinationRepository repository) {
        this.repository = repository;
    }

    /**
     * Erstellt ein neues Reiseziel mit Visa- und Impfanforderungen
     * 
     * @param dto Daten für das neue Reiseziel
     * @return DTO mit dem erstellten Reiseziel
     */
    @Transactional
    public TravelDestinationResponseDTO createDestination(CreateTravelDestinationDTO dto) {
        // Prüfe ob Ländercode bereits existiert
        if (repository.existsByCountryCode(dto.countryCode().toUpperCase())) {
            throw new InvalidCountryCodeException(
                dto.countryCode(), 
                "Reiseziel für diesen Ländercode existiert bereits"
            );
        }

        // Erstelle Domain-Objekte aus DTOs
        CountryCode countryCode;
        try {
            countryCode = new CountryCode(dto.countryCode());
        } catch (IllegalArgumentException e) {
            throw new InvalidCountryCodeException(dto.countryCode(), e.getMessage());
        }

        VisaRequirement visaRequirement = createVisaRequirement(dto.visaRequirement());
        VaccinationRequirement vaccinationRequirement = createVaccinationRequirement(dto.vaccinationRequirement());

        // Erstelle Entity (Business-Logik in Entity)
        TravelDestination destination = new TravelDestination(
            countryCode,
            dto.countryName(),
            visaRequirement,
            vaccinationRequirement,
            dto.generalTravelAdvice()
        );

        // Persistiere
        TravelDestination saved = repository.save(destination);

        return toResponseDTO(saved);
    }

    /**
     * Findet ein Reiseziel anhand des Ländercodes
     * 
     * @param countryCode ISO-Ländercode (z.B. "US", "CN", "IN")
     * @return DTO mit Reiseziel-Informationen
     * @throws DestinationNotFoundException wenn nicht gefunden
     */
    public TravelDestinationResponseDTO getDestinationByCountryCode(String countryCode) {
        TravelDestination destination = repository.findByCountryCode(countryCode.toUpperCase())
            .orElseThrow(() -> new DestinationNotFoundException(countryCode));

        return toResponseDTO(destination);
    }

    /**
     * Findet ein Reiseziel anhand der ID
     * 
     * @param id ID des Reiseziels
     * @return DTO mit Reiseziel-Informationen
     * @throws DestinationNotFoundException wenn nicht gefunden
     */
    public TravelDestinationResponseDTO getDestinationById(Long id) {
        TravelDestination destination = repository.findById(id)
            .orElseThrow(() -> new DestinationNotFoundException(id));

        return toResponseDTO(destination);
    }

    /**
     * Findet alle Reiseziele
     * 
     * @return Liste aller Reiseziele
     */
    public List<TravelDestinationResponseDTO> getAllDestinations() {
        return repository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    /**
     * Findet alle Reiseziele die Visa-Anforderungen haben
     * 
     * @return Liste der Reiseziele mit Visa-Pflicht
     */
    public List<TravelDestinationResponseDTO> getDestinationsRequiringVisa() {
        return repository.findByVisaRequired(true).stream()
            .map(this::toResponseDTO)
            .toList();
    }

    /**
     * Findet alle Reiseziele die Impfanforderungen haben
     * 
     * @return Liste der Reiseziele mit Impfpflicht
     */
    public List<TravelDestinationResponseDTO> getDestinationsRequiringVaccination() {
        return repository.findByVaccinationRequired(true).stream()
            .map(this::toResponseDTO)
            .toList();
    }

    /**
     * Aktualisiert die Visa-Anforderungen eines Reiseziels
     * 
     * DDD: Service delegiert zu Entity-Methode (updateVisaRequirement)
     * 
     * @param countryCode Ländercode
     * @param dto Neue Visa-Anforderungen
     * @return Aktualisiertes Reiseziel
     */
    @Transactional
    public TravelDestinationResponseDTO updateVisaRequirement(
        String countryCode, 
        UpdateVisaRequirementDTO dto
    ) {
        TravelDestination destination = repository.findByCountryCode(countryCode.toUpperCase())
            .orElseThrow(() -> new DestinationNotFoundException(countryCode));

        VisaRequirement newRequirement = createVisaRequirement(
            new VisaRequirementDTO(
                dto.required(), 
                dto.type(), 
                dto.processingDays(), 
                dto.notes()
            )
        );

        // Business-Methode der Entity aufrufen (NICHT selbst implementieren!)
        destination.updateVisaRequirement(newRequirement);

        TravelDestination updated = repository.save(destination);
        return toResponseDTO(updated);
    }

    /**
     * Aktualisiert die Impfanforderungen eines Reiseziels
     * 
     * DDD: Service delegiert zu Entity-Methode (updateVaccinationRequirement)
     * 
     * @param countryCode Ländercode
     * @param dto Neue Impfanforderungen
     * @return Aktualisiertes Reiseziel
     */
    @Transactional
    public TravelDestinationResponseDTO updateVaccinationRequirement(
        String countryCode, 
        UpdateVaccinationRequirementDTO dto
    ) {
        TravelDestination destination = repository.findByCountryCode(countryCode.toUpperCase())
            .orElseThrow(() -> new DestinationNotFoundException(countryCode));

        VaccinationRequirement newRequirement = createVaccinationRequirement(
            new VaccinationRequirementDTO(
                dto.required(), 
                dto.requiredVaccinations(), 
                dto.recommendedVaccinations(), 
                dto.notes()
            )
        );

        // Business-Methode der Entity aufrufen
        destination.updateVaccinationRequirement(newRequirement);

        TravelDestination updated = repository.save(destination);
        return toResponseDTO(updated);
    }

    /**
     * Aktualisiert allgemeine Reisehinweise
     * 
     * DDD: Service delegiert zu Entity-Methode
     * 
     * @param countryCode Ländercode
     * @param advice Neue Reisehinweise
     * @return Aktualisiertes Reiseziel
     */
    @Transactional
    public TravelDestinationResponseDTO updateTravelAdvice(String countryCode, String advice) {
        TravelDestination destination = repository.findByCountryCode(countryCode.toUpperCase())
            .orElseThrow(() -> new DestinationNotFoundException(countryCode));

        // Business-Methode der Entity aufrufen
        destination.updateGeneralTravelAdvice(advice);

        TravelDestination updated = repository.save(destination);
        return toResponseDTO(updated);
    }

    /**
     * Löscht ein Reiseziel
     * 
     * @param id ID des zu löschenden Reiseziels
     */
    @Transactional
    public void deleteDestination(Long id) {
        if (!repository.findById(id).isPresent()) {
            throw new DestinationNotFoundException(id);
        }
        repository.deleteById(id);
    }

    // ===== Private Helper-Methoden für DTO-Mapping =====

    /**
     * Erstellt VisaRequirement Value Object aus DTO
     */
    private VisaRequirement createVisaRequirement(VisaRequirementDTO dto) {
        if (!dto.required()) {
            return VisaRequirement.notRequired();
        }

        VisaType visaType = null;
        if (dto.type() != null) {
            try {
                visaType = VisaType.valueOf(dto.type().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Ungültiger Visum-Typ: " + dto.type() + 
                    ". Erlaubte Werte: BUSINESS, TOURIST, WORK, TRANSIT, E_VISA, VISA_ON_ARRIVAL"
                );
            }
        }

        return VisaRequirement.required(visaType, dto.processingDays(), dto.notes());
    }

    /**
     * Erstellt VaccinationRequirement Value Object aus DTO
     */
    private VaccinationRequirement createVaccinationRequirement(VaccinationRequirementDTO dto) {
        if (!dto.required() && (dto.requiredVaccinations() == null || dto.requiredVaccinations().isEmpty())) {
            if (dto.recommendedVaccinations() != null && !dto.recommendedVaccinations().isEmpty()) {
                return VaccinationRequirement.recommended(dto.recommendedVaccinations(), dto.notes());
            }
            return VaccinationRequirement.notRequired();
        }

        if (dto.required()) {
            return VaccinationRequirement.required(
                dto.requiredVaccinations(), 
                dto.recommendedVaccinations(), 
                dto.notes()
            );
        }

        return VaccinationRequirement.notRequired();
    }

    /**
     * Konvertiert Entity zu Response-DTO
     * 
     * DDD: DTOs nur für Datenübertragung, Entities bleiben im Domain Layer
     */
    private TravelDestinationResponseDTO toResponseDTO(TravelDestination entity) {
        VisaRequirementDTO visaDTO = new VisaRequirementDTO(
            entity.getVisaRequirement().isRequired(),
            entity.getVisaRequirement().getType() != null 
                ? entity.getVisaRequirement().getType().name() 
                : null,
            entity.getVisaRequirement().getProcessingDays(),
            entity.getVisaRequirement().getNotes()
        );

        VaccinationRequirementDTO vaccinationDTO = new VaccinationRequirementDTO(
            entity.getVaccinationRequirement().isRequired(),
            entity.getVaccinationRequirement().getRequiredVaccinations(),
            entity.getVaccinationRequirement().getRecommendedVaccinations(),
            entity.getVaccinationRequirement().getNotes()
        );

        return new TravelDestinationResponseDTO(
            entity.getId(),
            entity.getCountryCode().getCode(),
            entity.getCountryName(),
            entity.getCountryCode().isEuropeanUnion(),
            visaDTO,
            vaccinationDTO,
            entity.getGeneralTravelAdvice(),
            entity.requiresPreparation(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

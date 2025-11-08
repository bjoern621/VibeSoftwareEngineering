package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.AddTravelLegDTO;
import com.travelreimburse.application.dto.CreateTravelRequestDTO;
import com.travelreimburse.application.dto.TravelLegResponseDTO;
import com.travelreimburse.application.dto.TravelRequestResponseDTO;
import com.travelreimburse.domain.model.*;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service für Reiseanträge
 * Orchestriert den Use Case "Reise beantragen"
 */
@Service
@Transactional(readOnly = true)
public class TravelRequestService {
    
    private final TravelRequestRepository travelRequestRepository;

    public TravelRequestService(TravelRequestRepository travelRequestRepository) {
        this.travelRequestRepository = travelRequestRepository;
    }
    
    /**
     * Erstellt einen neuen Reiseantrag im Status DRAFT
     * @param dto die Daten für den Reiseantrag
     * @return der erstellte Reiseantrag als DTO
     */
    @Transactional
    public TravelRequestResponseDTO createTravelRequest(CreateTravelRequestDTO dto) {
        // DTO zu Domain-Objekten konvertieren
        DateRange travelPeriod = new DateRange(dto.startDate(), dto.endDate());
        com.travelreimburse.domain.model.Currency currency = 
            com.travelreimburse.domain.model.Currency.valueOf(dto.currency());
        Money estimatedCost = new Money(dto.estimatedAmount(), currency);
        CostCenter costCenter = new CostCenter(dto.costCenterCode(), dto.costCenterName());
        
        // Domain-Entity erstellen
        TravelRequest travelRequest = new TravelRequest(
            dto.employeeId(),
            costCenter,
            dto.destination(),
            dto.purpose(),
            travelPeriod,
            estimatedCost
        );
        
        // Persistieren
        TravelRequest saved = travelRequestRepository.save(travelRequest);
        
        // Entity zu DTO konvertieren
        return toResponseDTO(saved);
    }
    
    /**
     * Findet alle Reiseanträge eines Mitarbeiters
     * @param employeeId die ID des Mitarbeiters
     * @return Liste aller Reiseanträge als DTOs
     */
    public List<TravelRequestResponseDTO> findByEmployeeId(Long employeeId) {
        return travelRequestRepository.findByEmployeeId(employeeId)
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    /**
     * Findet einen Reiseantrag anhand seiner ID
     * @param id die ID des Reiseantrags
     * @return der Reiseantrag als DTO
     * @throws TravelRequestNotFoundException wenn nicht gefunden
     */
    public TravelRequestResponseDTO findById(Long id) {
        TravelRequest travelRequest = travelRequestRepository.findById(id)
            .orElseThrow(() -> new TravelRequestNotFoundException(id));
        return toResponseDTO(travelRequest);
    }
    
    /**
     * Reicht einen Reiseantrag ein (DRAFT -> SUBMITTED)
     * @param id die ID des Reiseantrags
     * @return der eingereichte Reiseantrag als DTO
     */
    @Transactional
    public TravelRequestResponseDTO submitTravelRequest(Long id) {
        TravelRequest travelRequest = travelRequestRepository.findById(id)
            .orElseThrow(() -> new TravelRequestNotFoundException(id));

        // ✅ DDD: Verwende semantisch korrekte Business-Methode statt generisches updateStatus()
        travelRequest.submit();

        // Persistieren (Spring Data publishes events automatically)
        TravelRequest saved = travelRequestRepository.save(travelRequest);

        return toResponseDTO(saved);
    }
    
    /**
     * Findet alle eingereichten Reiseanträge (zur Genehmigung)
     * @return Liste aller eingereichten Reiseanträge als DTOs
     */
    public List<TravelRequestResponseDTO> findPendingApprovals() {
        return travelRequestRepository.findPendingApprovals()
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    /**
     * Genehmigt einen Reiseantrag (SUBMITTED -> APPROVED)
     * @param id die ID des Reiseantrags
     * @param approverId die ID der genehmigenden Führungskraft
     * @return der genehmigte Reiseantrag als DTO
     */
    @Transactional
    public TravelRequestResponseDTO approveTravelRequest(Long id, Long approverId) {
        TravelRequest travelRequest = travelRequestRepository.findById(id)
            .orElseThrow(() -> new TravelRequestNotFoundException(id));

        // ✅ DDD: Verwende semantisch korrekte Business-Methode mit approverId
        travelRequest.approve(approverId);

        // Persistieren (Spring Data publishes events automatically)
        TravelRequest saved = travelRequestRepository.save(travelRequest);

        return toResponseDTO(saved);
    }

    /**
     * Lehnt einen Reiseantrag ab (SUBMITTED -> REJECTED)
     * @param id die ID des Reiseantrags
     * @param approverId die ID der ablehnenden Führungskraft
     * @param reason der Grund für die Ablehnung
     * @return der abgelehnte Reiseantrag als DTO
     */
    @Transactional
    public TravelRequestResponseDTO rejectTravelRequest(Long id, Long approverId, String reason) {
        TravelRequest travelRequest = travelRequestRepository.findById(id)
            .orElseThrow(() -> new TravelRequestNotFoundException(id));

        // ✅ DDD: Verwende semantisch korrekte Business-Methode mit approverId und reason
        travelRequest.reject(approverId, reason);

        // Persistieren (Spring Data publishes events automatically)
        TravelRequest saved = travelRequestRepository.save(travelRequest);


        return toResponseDTO(saved);
    }

    /**
     * Konvertiert eine TravelRequest-Entity zu einem DTO
     */
    private TravelRequestResponseDTO toResponseDTO(TravelRequest entity) {
        List<TravelLegResponseDTO> travelLegDTOs = entity.getTravelLegs().stream()
            .map(this::toTravelLegDTO)
            .toList();
        
        return new TravelRequestResponseDTO(
            entity.getId(),
            entity.getEmployeeId(),
            entity.getCostCenter() != null ? entity.getCostCenter().getCode() : null,
            entity.getCostCenter() != null ? entity.getCostCenter().getName() : null,
            entity.getDestination(),
            entity.getPurpose(),
            entity.getTravelPeriod().getStartDate(),
            entity.getTravelPeriod().getEndDate(),
            entity.getEstimatedCost().getAmount(),
            entity.getEstimatedCost().getCurrency().name(),
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getSubmittedAt(),
            entity.getApproverId(),
            entity.getApprovedAt(),
            entity.getRejectedAt(),
            entity.getRejectionReason(),
            travelLegDTOs
        );
    }
    
    /**
     * Konvertiert eine TravelLeg-Entity zu einem DTO
     */
    private TravelLegResponseDTO toTravelLegDTO(TravelLeg leg) {
        return new TravelLegResponseDTO(
            leg.getId(),
            leg.getDepartureLocation(),
            leg.getArrivalLocation(),
            leg.getTransportationType().name(),
            leg.getCost().getAmount(),
            leg.getCost().getCurrency().name(),
            leg.getDescription(),
            leg.getDepartureDateTime(),
            leg.getArrivalDateTime(),
            leg.getDistanceKm(),
            leg.getCreatedAt()
        );
    }
    
    /**
     * Fügt einen Reiseabschnitt zu einem Reiseantrag hinzu
     * Nur möglich im Status DRAFT
     * @param requestId die ID des Reiseantrags
     * @param dto die Daten für den Reiseabschnitt
     * @return der hinzugefügte Reiseabschnitt als DTO
     */
    @Transactional
    public TravelLegResponseDTO addTravelLeg(Long requestId, AddTravelLegDTO dto) {
        TravelRequest travelRequest = travelRequestRepository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));
        
        // DTO zu Domain-Objekten konvertieren
        TransportationType transportationType = TransportationType.valueOf(dto.transportationType());
        Currency currency = Currency.valueOf(dto.currency());
        Money cost = new Money(dto.costAmount(), currency);
        
        // Business-Logik aufrufen (Domain-Methode!)
        TravelLeg travelLeg = travelRequest.addTravelLeg(
            dto.departureLocation(),
            dto.arrivalLocation(),
            transportationType,
            cost
        );
        
        // ✅ DDD: Optionale Felder über Business-Methoden setzen
        if (dto.description() != null) {
            travelLeg.updateDescription(dto.description());
        }
        if (dto.departureDateTime() != null) {
            travelLeg.updateDepartureDateTime(dto.departureDateTime());
        }
        if (dto.arrivalDateTime() != null) {
            travelLeg.updateArrivalDateTime(dto.arrivalDateTime());
        }
        if (dto.distanceKm() != null) {
            travelLeg.updateDistanceKm(dto.distanceKm());
        }
        
        // Persistieren
        TravelRequest saved = travelRequestRepository.save(travelRequest);
        
        // Finde das neu hinzugefügte TravelLeg im gespeicherten Request
        TravelLeg savedLeg = saved.getTravelLegs().stream()
            .filter(leg -> leg.getDepartureLocation().equals(dto.departureLocation()) &&
                          leg.getArrivalLocation().equals(dto.arrivalLocation()) &&
                          leg.getTransportationType() == transportationType)
            .reduce((first, second) -> second) // Nimm das zuletzt hinzugefügte
            .orElseThrow(() -> new IllegalStateException("TravelLeg konnte nicht gespeichert werden"));
        
        return toTravelLegDTO(savedLeg);
    }
    
    /**
     * Fügt mehrere Reiseabschnitte zu einem Reiseantrag hinzu (Batch-Add)
     * Nur möglich im Status DRAFT
     * @param requestId die ID des Reiseantrags
     * @param dtos Liste von Reiseabschnitt-Daten
     * @return Liste der hinzugefügten Reiseabschnitte als DTOs
     */
    @Transactional
    public List<TravelLegResponseDTO> addTravelLegs(Long requestId, List<AddTravelLegDTO> dtos) {
        TravelRequest travelRequest = travelRequestRepository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));
        
        // Alle Travel Legs hinzufügen
        for (AddTravelLegDTO dto : dtos) {
            // DTO zu Domain-Objekten konvertieren
            TransportationType transportationType = TransportationType.valueOf(dto.transportationType());
            Currency currency = Currency.valueOf(dto.currency());
            Money cost = new Money(dto.costAmount(), currency);
            
            // Business-Logik aufrufen (Domain-Methode!)
            TravelLeg travelLeg = travelRequest.addTravelLeg(
                dto.departureLocation(),
                dto.arrivalLocation(),
                transportationType,
                cost
            );
            
            // ✅ DDD: Optionale Felder über Business-Methoden setzen
            if (dto.description() != null) {
                travelLeg.updateDescription(dto.description());
            }
            if (dto.departureDateTime() != null) {
                travelLeg.updateDepartureDateTime(dto.departureDateTime());
            }
            if (dto.arrivalDateTime() != null) {
                travelLeg.updateArrivalDateTime(dto.arrivalDateTime());
            }
            if (dto.distanceKm() != null) {
                travelLeg.updateDistanceKm(dto.distanceKm());
            }
        }
        
        // Persistieren
        TravelRequest saved = travelRequestRepository.save(travelRequest);
        
        // Alle TravelLegs zurückgeben
        return saved.getTravelLegs().stream()
            .map(this::toTravelLegDTO)
            .toList();
    }
    
    /**
     * Entfernt einen Reiseabschnitt von einem Reiseantrag
     * Nur möglich im Status DRAFT
     * @param requestId die ID des Reiseantrags
     * @param legId die ID des zu entfernenden Reiseabschnitts
     */
    @Transactional
    public void removeTravelLeg(Long requestId, Long legId) {
        TravelRequest travelRequest = travelRequestRepository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));
        
        // Business-Logik aufrufen (Domain-Methode!)
        travelRequest.removeTravelLegById(legId);
        
        // Persistieren
        travelRequestRepository.save(travelRequest);
    }
    
    /**
     * Gibt alle Reiseabschnitte eines Reiseantrags zurück
     * @param requestId die ID des Reiseantrags
     * @return Liste aller Reiseabschnitte als DTOs
     */
    public List<TravelLegResponseDTO> getTravelLegsByRequestId(Long requestId) {
        TravelRequest travelRequest = travelRequestRepository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));
        
        return travelRequest.getTravelLegs().stream()
            .map(this::toTravelLegDTO)
            .toList();
    }
}

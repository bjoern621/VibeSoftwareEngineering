package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.CreateTravelRequestDTO;
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
        Currency currency = Currency.valueOf(dto.currency());
        Money estimatedCost = new Money(dto.estimatedAmount(), currency);
        
        // Domain-Entity erstellen
        TravelRequest travelRequest = new TravelRequest(
            dto.employeeId(),
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
        
        // Business-Logik aufrufen (Domain-Methode!)
        travelRequest.submit();
        
        // Persistieren
        TravelRequest saved = travelRequestRepository.save(travelRequest);
        
        return toResponseDTO(saved);
    }
    
    /**
     * Konvertiert eine TravelRequest-Entity zu einem DTO
     */
    private TravelRequestResponseDTO toResponseDTO(TravelRequest entity) {
        return new TravelRequestResponseDTO(
            entity.getId(),
            entity.getEmployeeId(),
            entity.getDestination(),
            entity.getPurpose(),
            entity.getTravelPeriod().getStartDate(),
            entity.getTravelPeriod().getEndDate(),
            entity.getEstimatedCost().getAmount(),
            entity.getEstimatedCost().getCurrency().name(),
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getSubmittedAt()
        );
    }
}

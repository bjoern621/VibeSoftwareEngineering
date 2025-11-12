package com.travelreimburse.application.service;

import com.travelreimburse.domain.exception.TravelRequestNotFoundException;
import com.travelreimburse.domain.exception.CannotArchiveTravelRequestException;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Application Service für Archivierung.
 * Orchestriert Use Cases - KEINE Business-Logik!
 */
@Service
@Transactional(readOnly = true)
public class ArchivingService {
    
    private final TravelRequestRepository repository;
    
    public ArchivingService(TravelRequestRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Use Case: Einzelne Reise archivieren
     */
    @Transactional
    public TravelRequest archiveTravelRequest(Long requestId) {
        TravelRequest request = repository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));

        // Business-Logik in Entity!
        request.archive();

        return repository.save(request);
    }

    /**
     * Use Case: Reise mit benutzerdefinierter Frist archivieren
     */
    @Transactional
    public TravelRequest archiveWithCustomRetention(Long requestId, int retentionYears) {
        TravelRequest request = repository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));

        request.archiveWithCustomRetention(retentionYears);

        return repository.save(request);
    }

    /**
     * Use Case: Alle bereiten Reisen automatisch archivieren (Batch)
     */
    @Transactional
    public int archiveAllReadyRequests() {
        List<TravelRequest> readyForArchiving = repository.findAllReadyForArchiving();
        
        int archivedCount = 0;
        for (TravelRequest request : readyForArchiving) {
            try {
                request.archive();
                repository.save(request);
                archivedCount++;
            } catch (CannotArchiveTravelRequestException e) {
                // Log und weiter (skip)
            }
        }
        
        return archivedCount;
    }
    
    /**
     * Query: Finde Reisen mit abgelaufener Frist
     */
    public List<TravelRequest> findExpiredRetentionRequests() {
        return repository.findAllWithExpiredRetention();
    }
    
    /**
     * Query: Archivierte Reisen in Zeitraum
     */
    public List<TravelRequest> findArchivedInPeriod(LocalDate start, LocalDate end) {
        return repository.findArchivedBetween(start, end);
    }

    /**
     * Use Case: Archiviere Reise wenn berechtigt
     * Kann manuell aufgerufen werden oder von einem Scheduler.
     * Flow: TravelRequest.status = PAID → Eligibility Check → Archivierung
     * Invariante: TravelRequest muss Status PAID haben
     */
    @Transactional
    public void archiveTravelRequestIfEligible(Long travelRequestId) {
        TravelRequest request = repository.findById(travelRequestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(travelRequestId));

        // Eligibility Check
        if (!isEligibleForArchiving(request)) {
            throw new CannotArchiveTravelRequestException(
                travelRequestId,
                String.format("Nicht berechtigt für Archivierung (Status: %s)", request.getStatus())
            );
        }

        // Archiviere mit Standard-Frist (10 Jahre)
        request.archive();
        repository.save(request);
    }

    /**
     * Business Query: Prüft ob TravelRequest archiviert werden kann
     */
    private boolean isEligibleForArchiving(TravelRequest request) {
        // Muss bezahlt sein
        return request.isPaid();
        // Später könnte man erweitern:
        // && hasAllReceiptsSubmitted(request)
        // && meetsRetentionRequirements(request)
    }
}


package com.travelreimburse.application.service;

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
     * Use Case: Archiviere Reise nach erfolgreicher Zahlung
     * Wird vom PaymentEventHandler aufgerufen, wenn PaymentSuccessEvent publishet wird.
     * Flow: Payment erfolgreich → TravelRequest.status = PAID → Archivierung
     * Invariante: TravelRequest muss Status PAID haben
     */
    @Transactional
    public void archiveAfterPaymentSuccess(Long travelRequestId) {
        TravelRequest request = repository.findById(travelRequestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(travelRequestId));

        // Prüfe dass Payment erfolgreich war (Status = PAID)
        if (!request.isPaid()) {
            throw new IllegalArgumentException(
                String.format("TravelRequest %d muss Status PAID haben, aktuell: %s",
                    travelRequestId, request.getStatus())
            );
        }

        try {
            // Archiviere mit Standard-Frist (10 Jahre)
            request.archive();
            repository.save(request);
        } catch (CannotArchiveTravelRequestException e) {
            // Log Fehler aber werfe Exception nicht - Payment ist bereits erfolg!
            throw new IllegalStateException(
                String.format("Archivierung fehlgeschlagen für TravelRequest %d: %s",
                    travelRequestId, e.getMessage())
            );
        }
    }
}


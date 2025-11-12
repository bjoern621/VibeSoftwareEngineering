package com.travelreimburse.application.service;

import com.travelreimburse.domain.exception.TravelRequestNotFoundException;
import com.travelreimburse.domain.exception.CannotArchiveTravelRequestException;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.repository.PaymentRequestRepository;
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
    
    private final TravelRequestRepository travelRequestRepository;
    private final PaymentRequestRepository paymentRequestRepository;

    public ArchivingService(
        TravelRequestRepository travelRequestRepository,
        PaymentRequestRepository paymentRequestRepository
    ) {
        this.travelRequestRepository = travelRequestRepository;
        this.paymentRequestRepository = paymentRequestRepository;
    }
    
    /**
     * Use Case: Einzelne Reise archivieren
     * Koordiniert Archivierung von TravelRequest UND zugehörigem PaymentRequest
     */
    @Transactional
    public TravelRequest archiveTravelRequest(Long requestId) {
        TravelRequest request = travelRequestRepository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));

        // 1. Archiviere TravelRequest Aggregate
        request.archive();
        TravelRequest savedRequest = travelRequestRepository.save(request);

        // 2. Archiviere zugehöriges PaymentRequest Aggregate (falls vorhanden)
        archiveRelatedPayment(requestId, 10);

        return savedRequest;
    }

    /**
     * Use Case: Reise mit benutzerdefinierter Frist archivieren
     */
    @Transactional
    public TravelRequest archiveWithCustomRetention(Long requestId, int retentionYears) {
        TravelRequest request = travelRequestRepository.findById(requestId)
            .orElseThrow(() -> new TravelRequestNotFoundException(requestId));

        request.archiveWithCustomRetention(retentionYears);
        TravelRequest savedRequest = travelRequestRepository.save(request);

        // Archiviere Payment mit gleicher Frist
        archiveRelatedPayment(requestId, retentionYears);

        return savedRequest;
    }

    /**
     * Use Case: Alle bereiten Reisen automatisch archivieren (Batch)
     */
    @Transactional
    public int archiveAllReadyRequests() {
        List<TravelRequest> readyForArchiving = travelRequestRepository.findAllReadyForArchiving();

        int archivedCount = 0;
        for (TravelRequest request : readyForArchiving) {
            try {
                request.archive();
                travelRequestRepository.save(request);

                // Archiviere zugehöriges Payment
                archiveRelatedPayment(request.getId(), 10);

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
        return travelRequestRepository.findAllWithExpiredRetention();
    }
    
    /**
     * Query: Archivierte Reisen in Zeitraum
     */
    public List<TravelRequest> findArchivedInPeriod(LocalDate start, LocalDate end) {
        return travelRequestRepository.findArchivedBetween(start, end);
    }

    /**
     * Use Case: Archiviere Reise wenn berechtigt
     * Kann manuell aufgerufen werden oder von einem Scheduler.
     * Flow: TravelRequest.status = PAID → Eligibility Check → Archivierung
     * Invariante: TravelRequest muss Status PAID haben
     */
    @Transactional
    public void archiveTravelRequestIfEligible(Long travelRequestId) {
        TravelRequest request = travelRequestRepository.findById(travelRequestId)
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
        travelRequestRepository.save(request);

        // Archiviere zugehöriges Payment
        archiveRelatedPayment(travelRequestId, 10);
    }

    /**
     * Private Helper: Archiviert PaymentRequest für TravelRequest
     * Koordiniert zwei separate Aggregates (Domain Service Pattern!)
     */
    private void archiveRelatedPayment(Long travelRequestId, int retentionYears) {
        paymentRequestRepository.findByTravelRequestId(travelRequestId)
            .ifPresent(payment -> {
                if (payment.canBeArchived()) {
                    payment.archive(retentionYears);
                    paymentRequestRepository.save(payment);
                }
            });
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


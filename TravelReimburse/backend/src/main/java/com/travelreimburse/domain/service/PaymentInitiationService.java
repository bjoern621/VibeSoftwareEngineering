package com.travelreimburse.domain.service;

import com.travelreimburse.domain.exception.CannotSubmitPaymentException;
import com.travelreimburse.domain.model.PaymentRequest;
import com.travelreimburse.domain.model.TravelRequest;
import com.travelreimburse.domain.model.TravelRequestStatus;
import com.travelreimburse.domain.repository.PaymentRequestRepository;
import com.travelreimburse.domain.repository.TravelRequestRepository;
import org.springframework.stereotype.Service;

/**
 * Domain Service für Payment-Initiierung.
 * Orchestriert komplexe Business-Logik, die nicht zu einer einzelnen Entity gehört.
 *
 * WICHTIG: Das ist ein Domain Service, KEINE Application Service!
 * Er bleibt im Domain Layer und wird von Application Services verwendet.
 */
@Service
public class PaymentInitiationService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final TravelRequestRepository travelRequestRepository;

    public PaymentInitiationService(
            PaymentRequestRepository paymentRequestRepository,
            TravelRequestRepository travelRequestRepository) {
        this.paymentRequestRepository = paymentRequestRepository;
        this.travelRequestRepository = travelRequestRepository;
    }

    /**
     * Validiert, ob ein TravelRequest bezahlt werden kann.
     *
     * Invarianten:
     *  - Status muss APPROVED sein
     *  - Es darf keine bereits laufende Payment existieren
     */
    public void validateCanPayTravelRequest(TravelRequest travelRequest) {
        // 1. Status-Validierung
        if (travelRequest.getStatus() != TravelRequestStatus.APPROVED) {
            throw new CannotSubmitPaymentException(
                "Reiseantrag muss Status APPROVED haben (aktuell: " + travelRequest.getStatus() + ")"
            );
        }

        // 2. Prüfe ob bereits Payment existiert
        if (travelRequestRepository.findById(travelRequest.getId()).isPresent()) {
            var existingPayment = paymentRequestRepository.findByTravelRequestId(travelRequest.getId());
            if (existingPayment.isPresent() && !existingPayment.get().hasFailed()) {
                throw new CannotSubmitPaymentException(
                    "Für diesen Reiseantrag existiert bereits ein aktiver Payment"
                );
            }
        }
    }

    /**
     * Erstellt einen neuen PaymentRequest mit berechneter Gesamtsumme.
     *
     * Schritte:
     *  1. Validiere
     *  2. Erstelle PaymentRequest mit Status PENDING
     *  3. Return PaymentRequest (wird vom Service gespeichert)
     */
    public PaymentRequest createPaymentRequest(TravelRequest travelRequest) {
        validateCanPayTravelRequest(travelRequest);
        return PaymentRequest.create(travelRequest, travelRequest.getEstimatedCost());
    }

    /**
     * Query-Methode: Findet einen zahlungsbereiten TravelRequest
     */
    public boolean canTravelRequestBePaid(Long travelRequestId) {
        var travelRequest = travelRequestRepository.findById(travelRequestId);
        if (travelRequest.isEmpty()) {
            return false;
        }

        try {
            validateCanPayTravelRequest(travelRequest.get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


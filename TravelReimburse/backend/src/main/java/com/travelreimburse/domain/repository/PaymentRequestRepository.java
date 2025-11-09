package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.PaymentReference;
import com.travelreimburse.domain.model.PaymentRequest;
import com.travelreimburse.domain.model.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für PaymentRequests.
 * Domain Layer Abstraction - keine Implementation hier!
 */
public interface PaymentRequestRepository {

    /**
     * Speichert einen PaymentRequest
     */
    PaymentRequest save(PaymentRequest paymentRequest);

    /**
     * Findet einen PaymentRequest nach ID
     */
    Optional<PaymentRequest> findById(Long id);

    /**
     * Findet einen PaymentRequest nach TravelRequest-ID
     */
    Optional<PaymentRequest> findByTravelRequestId(Long travelRequestId);

    /**
     * Findet einen PaymentRequest nach PaymentReference
     */
    Optional<PaymentRequest> findByPaymentReference(PaymentReference paymentReference);

    /**
     * Findet alle PaymentRequests mit einem bestimmten Status
     */
    List<PaymentRequest> findAllWithStatus(PaymentStatus status);

}


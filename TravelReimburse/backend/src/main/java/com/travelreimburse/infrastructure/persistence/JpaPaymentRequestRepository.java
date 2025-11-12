package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.PaymentReference;
import com.travelreimburse.domain.model.PaymentRequest;
import com.travelreimburse.domain.model.PaymentStatus;
import com.travelreimburse.domain.repository.PaymentRequestRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository für PaymentRequest Persistence.
 * Infrastructure Layer - implementiert Domain Repository Interface.
 */
@Repository
public interface JpaPaymentRequestRepository
    extends PaymentRequestRepository, JpaRepository<PaymentRequest, Long> {

    @Override
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.travelRequest.id = :travelRequestId")
    Optional<PaymentRequest> findByTravelRequestId(@Param("travelRequestId") Long travelRequestId);

    @Override
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.paymentReference = :paymentReference")
    Optional<PaymentRequest> findByPaymentReference(@Param("paymentReference") PaymentReference paymentReference);

    @Override
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = :status")
    List<PaymentRequest> findAllWithStatus(@Param("status") PaymentStatus status);

    /**
     * Spring Data JPA method - findet alle Payments mit einem Status
     */
    List<PaymentRequest> findByStatus(PaymentStatus status);

}


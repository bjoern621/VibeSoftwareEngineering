package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.Receipt;
import com.travelreimburse.domain.model.ReceiptStatus;
import com.travelreimburse.domain.model.ReceiptType;
import com.travelreimburse.domain.repository.ReceiptRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA-Repository-Implementierung für Receipt.
 * Erweitert sowohl das Domain-Repository-Interface als auch JpaRepository.
 */
@Repository
public interface JpaReceiptRepository extends ReceiptRepository, JpaRepository<Receipt, Long> {

    // Spring Data JPA generiert die Implementierung automatisch
    @Override
    List<Receipt> findByTravelRequestId(Long travelRequestId);

    @Override
    List<Receipt> findByStatus(ReceiptStatus status);

    @Override
    List<Receipt> findByType(ReceiptType type);

    @Override
    List<Receipt> findByTravelRequestIdAndStatus(Long travelRequestId, ReceiptStatus status);

    @Override
    long countByTravelRequestId(Long travelRequestId);
}


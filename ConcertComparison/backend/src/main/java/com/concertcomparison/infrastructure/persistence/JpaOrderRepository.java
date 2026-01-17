package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Order;
import com.concertcomparison.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-Implementierung des OrderRepository (Adapter).
 * 
 * <p>Erweitert Spring Data JpaRepository und implementiert das
 * OrderRepository-Interface aus dem Domain-Layer.</p>
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {

    /**
     * {@inheritDoc}
     * 
     * <p>Spring Data generiert automatisch die Query basierend auf dem Method-Namen.</p>
     */
    @Override
    List<Order> findByUserId(String userId);

    /**
     * {@inheritDoc}
     * 
     * <p>Spring Data generiert automatisch die Query basierend auf dem Method-Namen.</p>
     */
    @Override
    Optional<Order> findBySeatId(Long seatId);

    /**
     * {@inheritDoc}
     * 
     * <p>Spring Data generiert automatisch die Query basierend auf dem Method-Namen.</p>
     */
    @Override
    long countByUserId(String userId);
}

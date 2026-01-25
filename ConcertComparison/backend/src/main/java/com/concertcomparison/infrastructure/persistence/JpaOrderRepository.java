package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.Order;
import com.concertcomparison.domain.model.OrderStatus;
import com.concertcomparison.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Implementierung des OrderRepository.
 * 
 * Infrastructure Layer: Implementiert das Domain Repository Interface.
 * Verwendet Spring Data JPA für automatische Query-Generierung.
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {
    
    // Spring Data JPA generiert automatisch Queries
    
    /**
     * Findet alle Orders eines Users, sortiert nach Kaufdatum absteigend.
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.purchaseDate DESC")
    @Override
    List<Order> findByUserId(@Param("userId") String userId);
    
    @Override
    List<Order> findBySeatId(Long seatId);
    
    @Override
    Optional<Order> findBySeatIdAndStatus(Long seatId, OrderStatus status);
    
    /**
     * Findet alle Orders eines Users nach Status.
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.purchaseDate DESC")
    @Override
    List<Order> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") OrderStatus status);
    
    @Override
    List<Order> findByStatus(OrderStatus status);
    
    @Override
    boolean existsBySeatId(Long seatId);
}

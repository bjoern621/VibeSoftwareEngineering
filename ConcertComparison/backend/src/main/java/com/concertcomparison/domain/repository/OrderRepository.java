package com.concertcomparison.domain.repository;

import com.concertcomparison.domain.model.Order;
import com.concertcomparison.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Order Aggregate Root.
 * 
 * DDD Port (Interface im Domain Layer, Implementierung in Infrastructure).
 * Definiert alle Datenbank-Operationen für Orders.
 */
public interface OrderRepository {
    
    /**
     * Speichert eine Order (Create oder Update).
     * 
     * @param order Zu speichernde Order
     * @return Gespeicherte Order mit generierter ID
     */
    Order save(Order order);
    
    /**
     * Sucht eine Order anhand der ID.
     * 
     * @param id Order-ID
     * @return Optional mit Order, oder empty wenn nicht gefunden
     */
    Optional<Order> findById(Long id);
    
    /**
     * Liefert alle Orders.
     * 
     * @return Liste aller Orders
     */
    List<Order> findAll();
    
    /**
     * Sucht alle Orders eines Users.
     * 
     * @param userId User-ID
     * @return Liste der Orders des Users, sortiert nach Kaufdatum absteigend
     */
    List<Order> findByUserId(String userId);
    
    /**
     * Sucht alle Orders für einen Seat.
     * 
     * Business Rule: Es sollte maximal eine Order pro Seat geben.
     * 
     * @param seatId Seat-ID
     * @return Liste der Orders für den Seat
     */
    List<Order> findBySeatId(Long seatId);
    
    /**
     * Sucht eine Order für einen Seat (sollte maximal eine sein).
     * 
     * @param seatId Seat-ID
     * @return Optional mit Order, oder empty wenn nicht gefunden
     */
    Optional<Order> findBySeatIdAndStatus(Long seatId, OrderStatus status);
    
    /**
     * Sucht alle Orders eines Users nach Status.
     * 
     * @param userId User-ID
     * @param status Order-Status
     * @return Liste der Orders
     */
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);
    
    /**
     * Sucht alle Orders nach Status.
     * 
     * @param status Order-Status
     * @return Liste der Orders
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Löscht eine Order anhand der ID.
     * 
     * ACHTUNG: Normalerweise sollten Orders nicht gelöscht, sondern storniert werden.
     * 
     * @param id Order-ID
     */
    void deleteById(Long id);
    
    /**
     * Prüft ob eine Order mit der ID existiert.
     * 
     * @param id Order-ID
     * @return true wenn Order existiert
     */
    boolean existsById(Long id);
    
    /**
     * Prüft ob bereits eine Order für einen Seat existiert.
     * 
     * @param seatId Seat-ID
     * @return true wenn Order existiert
     */
    boolean existsBySeatId(Long seatId);
}

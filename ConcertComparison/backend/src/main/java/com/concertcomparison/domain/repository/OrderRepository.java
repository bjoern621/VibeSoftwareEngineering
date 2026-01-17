package com.concertcomparison.domain.repository;

import com.concertcomparison.domain.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * Repository-Interface für Order-Aggregate (Port).
 * 
 * <p>Definiert die Persistierungs-Operationen für Orders.
 * Die Implementierung erfolgt im Infrastructure-Layer (Adapter).</p>
 */
public interface OrderRepository {

    /**
     * Speichert eine Order (Create oder Update).
     * 
     * @param order Die zu speichernde Order
     * @return Die gespeicherte Order mit generierter ID (bei neuen Orders)
     */
    Order save(Order order);

    /**
     * Sucht eine Order anhand ihrer ID.
     * 
     * @param id Die Order-ID
     * @return Optional mit Order, wenn gefunden
     */
    Optional<Order> findById(Long id);

    /**
     * Findet alle Orders eines bestimmten Users.
     * 
     * @param userId Die User-ID
     * @return Liste aller Orders des Users (leer, wenn keine vorhanden)
     */
    List<Order> findByUserId(String userId);

    /**
     * Findet eine Order anhand der Seat-ID.
     * 
     * @param seatId Die Seat-ID
     * @return Optional mit Order, wenn gefunden
     */
    Optional<Order> findBySeatId(Long seatId);

    /**
     * Zählt alle Orders eines Users.
     * 
     * @param userId Die User-ID
     * @return Anzahl der Orders
     */
    long countByUserId(String userId);

    /**
     * Löscht eine Order.
     * 
     * @param order Die zu löschende Order
     */
    void delete(Order order);
}

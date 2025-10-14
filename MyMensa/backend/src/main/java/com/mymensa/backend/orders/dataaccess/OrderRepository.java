package com.mymensa.backend.orders.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    /**
     * Findet eine Bestellung anhand des QR-Codes
     */
    Optional<Order> findByQrCode(String qrCode);
}

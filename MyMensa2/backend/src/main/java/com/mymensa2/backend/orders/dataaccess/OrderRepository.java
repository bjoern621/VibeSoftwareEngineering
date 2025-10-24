package com.mymensa2.backend.orders.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // Findet Bestellung anhand des QR-Codes
    Optional<Order> findByQrCode(String qrCode);
    
    // Findet alle Bestellungen in einem Zeitraum (basierend auf Abholdatum)
    @Query("SELECT o FROM Order o WHERE o.pickupDate BETWEEN :startDate AND :endDate")
    List<Order> findByPickupDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Findet alle bezahlten Bestellungen
    @Query("SELECT o FROM Order o WHERE o.paid = true")
    List<Order> findAllPaid();
    
    // Findet alle abgeholten Bestellungen
    @Query("SELECT o FROM Order o WHERE o.collected = true")
    List<Order> findAllCollected();
}

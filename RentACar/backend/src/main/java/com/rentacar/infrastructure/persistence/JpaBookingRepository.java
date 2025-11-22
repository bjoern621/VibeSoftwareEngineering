package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.BookingStatus;
import com.rentacar.domain.repository.BookingRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository Implementation für Booking.
 * 
 * Erweitert sowohl das Domain Repository Interface als auch JpaRepository.
 * Spring Data JPA leitet die Implementierung automatisch ab.
 */
@Repository
public interface JpaBookingRepository extends BookingRepository, JpaRepository<Booking, Long> {

    /**
     * Findet alle Buchungen eines Kunden, chronologisch sortiert (neueste zuerst).
     * Spring Data JPA leitet die Query aus dem Methodennamen ab.
     */
    @Override
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Findet Buchungen eines Kunden nach Status, chronologisch sortiert.
     * Spring Data JPA leitet die Query aus dem Methodennamen ab.
     */
    @Override
    List<Booking> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, BookingStatus status);

    /**
     * Findet alle aktiven Buchungen (REQUESTED oder CONFIRMED) für ein Fahrzeug.
     * Verwendet explizite JPQL-Query für komplexere Logik.
     */
    @Override
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN ('REQUESTED', 'CONFIRMED')")
    List<Booking> findActiveBookingsByVehicleId(@Param("vehicleId") Long vehicleId);
}

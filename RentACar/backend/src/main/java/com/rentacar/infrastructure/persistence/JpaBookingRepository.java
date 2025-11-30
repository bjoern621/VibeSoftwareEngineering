package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.BookingStatus;
import com.rentacar.domain.repository.BookingRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
     * Findet alle Buchungen eines Kunden, sortiert nach Abholdatum (früheste zuerst).
     * Spring Data JPA leitet die Query aus dem Methodennamen ab.
     */
    @Override
    List<Booking> findByCustomerIdOrderByPickupDateTimeAsc(Long customerId);

    /**
     * Findet Buchungen eines Kunden nach Status, sortiert nach Abholdatum (früheste zuerst).
     * Spring Data JPA leitet die Query aus dem Methodennamen ab.
     */
    @Override
    List<Booking> findByCustomerIdAndStatusOrderByPickupDateTimeAsc(Long customerId, BookingStatus status);

    /**
     * Findet alle aktiven Buchungen (REQUESTED oder CONFIRMED) für ein Fahrzeug.
     * Verwendet explizite JPQL-Query für komplexere Logik.
     */
    @Override
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN ('REQUESTED', 'CONFIRMED')")
    List<Booking> findActiveBookingsByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Findet überlappende Buchungen für ein Fahrzeug.
     * Verwendet explizite JPQL-Query für komplexere Logik.
     */
    @Override
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "AND ((b.pickupDateTime < :end) AND (b.returnDateTime > :start))")
    List<Booking> findOverlappingBookings(@Param("vehicleId") Long vehicleId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}

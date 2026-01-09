package com.rentacar.domain.repository;

import com.rentacar.domain.model.Booking;
import com.rentacar.domain.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Booking Aggregate.
 * 
 * Definiert Methoden zum Laden und Speichern von Buchungen.
 * Implementation erfolgt in der Infrastructure-Schicht.
 */
public interface BookingRepository {
    
    /**
     * Speichert eine Buchung.
     * 
     * @param booking Die zu speichernde Buchung
     * @return Die gespeicherte Buchung
     */
    Booking save(Booking booking);
    
    /**
     * Findet eine Buchung anhand der ID.
     * 
     * @param id Die Buchungs-ID
     * @return Optional mit der Buchung, falls gefunden
     */
    Optional<Booking> findById(Long id);
    
    /**
     * Findet alle Buchungen eines Kunden, sortiert nach Abholdatum (früheste zuerst).
     * 
     * @param customerId Die Kunden-ID
     * @return Liste aller Buchungen des Kunden
     */
    List<Booking> findByCustomerIdOrderByPickupDateTimeAsc(Long customerId);
    
    /**
     * Findet Buchungen eines Kunden nach Status, sortiert nach Abholdatum (früheste zuerst).
     * 
     * @param customerId Die Kunden-ID
     * @param status Der gewünschte Buchungsstatus
     * @return Liste der Buchungen mit dem angegebenen Status
     */
    List<Booking> findByCustomerIdAndStatusOrderByPickupDateTimeAsc(Long customerId, BookingStatus status);
    
    /**
     * Findet alle aktiven Buchungen für ein Fahrzeug.
     * 
     * @param vehicleId Die Fahrzeug-ID
     * @return Liste aktiver Buchungen für das Fahrzeug
     */
    List<Booking> findActiveBookingsByVehicleId(Long vehicleId);
    
    /**
     * Prüft, ob eine Buchung mit der gegebenen ID existiert.
     * 
     * @param id Die Buchungs-ID
     * @return true wenn die Buchung existiert
     */
    boolean existsById(Long id);
    
    /**
     * Findet überlappende Buchungen für ein Fahrzeug in einem Zeitraum.
     * 
     * @param vehicleId Die Fahrzeug-ID
     * @param start Startzeitpunkt
     * @param end Endzeitpunkt
     * @return Liste der überlappenden Buchungen
     */
    List<Booking> findOverlappingBookings(Long vehicleId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Findet alle Buchungen, sortiert nach Abholdatum (früheste zuerst).
     * Für Employee/Admin Zugriff.
     * 
     * @return Liste aller Buchungen
     */
    List<Booking> findAllByOrderByPickupDateTimeAsc();
    
    /**
     * Findet alle Buchungen nach Status, sortiert nach Abholdatum (früheste zuerst).
     * Für Employee/Admin Zugriff.
     * 
     * @param status Der gewünschte Buchungsstatus
     * @return Liste der Buchungen mit dem angegebenen Status
     */
    List<Booking> findByStatusOrderByPickupDateTimeAsc(BookingStatus status);
}

package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.TravelDestination;
import java.util.List;
import java.util.Optional;

/**
 * Repository-Interface für TravelDestination (Domain Layer)
 * Definiert die Persistierungs-Operationen ohne technische Details
 * Die konkrete Implementierung erfolgt im Infrastructure Layer
 * 
 * DDD: Repository-Interface im Domain Layer (abstrakt)
 */
public interface TravelDestinationRepository {
    
    /**
     * Speichert ein Reiseziel (erstellt neu oder aktualisiert bestehendes)
     * 
     * @param destination das zu speichernde Reiseziel
     * @return das gespeicherte Reiseziel mit ID
     */
    TravelDestination save(TravelDestination destination);
    
    /**
     * Findet ein Reiseziel anhand seiner ID
     * 
     * @param id die ID des Reiseziels
     * @return Optional mit dem Reiseziel, oder leer wenn nicht gefunden
     */
    Optional<TravelDestination> findById(Long id);
    
    /**
     * Findet ein Reiseziel anhand des Ländercodes
     * 
     * @param countryCode ISO 3166-1 Alpha-2 Ländercode (z.B. "DE", "US")
     * @return Optional mit dem Reiseziel, oder leer wenn nicht gefunden
     */
    Optional<TravelDestination> findByCountryCode(String countryCode);
    
    /**
     * Findet alle Reiseziele
     * 
     * @return Liste aller Reiseziele
     */
    List<TravelDestination> findAll();
    
    /**
     * Findet alle Reiseziele die Visa-Anforderungen haben
     * 
     * @return Liste der Reiseziele mit Visa-Pflicht
     */
    List<TravelDestination> findByVisaRequired(boolean required);
    
    /**
     * Findet alle Reiseziele die Impfanforderungen haben
     * 
     * @return Liste der Reiseziele mit Impfpflicht
     */
    List<TravelDestination> findByVaccinationRequired(boolean required);
    
    /**
     * Prüft ob ein Reiseziel für einen Ländercode existiert
     * 
     * @param countryCode ISO 3166-1 Alpha-2 Ländercode
     * @return true wenn vorhanden, false wenn nicht
     */
    boolean existsByCountryCode(String countryCode);
    
    /**
     * Löscht ein Reiseziel anhand seiner ID
     * 
     * @param id die ID des zu löschenden Reiseziels
     */
    void deleteById(Long id);
}

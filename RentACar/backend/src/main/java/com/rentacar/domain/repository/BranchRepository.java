package com.rentacar.domain.repository;

import com.rentacar.domain.model.Branch;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für Branch (Filiale).
 * 
 * Port-Interface im Sinne der Hexagonalen Architektur.
 * Wird in der Infrastructure-Schicht implementiert.
 */
public interface BranchRepository {
    
    /**
     * Speichert eine Filiale.
     * 
     * @param branch die zu speichernde Filiale
     * @return die gespeicherte Filiale
     */
    Branch save(Branch branch);
    
    /**
     * Findet eine Filiale anhand ihrer ID.
     * 
     * @param id die ID der Filiale
     * @return Optional mit der Filiale, falls vorhanden
     */
    Optional<Branch> findById(Long id);
    
    /**
     * Findet eine Filiale anhand ihres Namens.
     * 
     * @param name der Name der Filiale
     * @return Optional mit der Filiale, falls vorhanden
     */
    Optional<Branch> findByName(String name);
    
    /**
     * Gibt alle Filialen zurück.
     * 
     * @return Liste aller Filialen
     */
    List<Branch> findAll();
    
    /**
     * Löscht eine Filiale.
     * 
     * @param branch die zu löschende Filiale
     */
    void delete(Branch branch);
    
    /**
     * Prüft, ob eine Filiale mit dem gegebenen Namen existiert.
     * 
     * @param name der Name der Filiale
     * @return true wenn eine Filiale mit diesem Namen existiert
     */
    boolean existsByName(String name);
}

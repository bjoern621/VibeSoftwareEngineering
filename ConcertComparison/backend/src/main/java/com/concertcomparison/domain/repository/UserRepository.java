package com.concertcomparison.domain.repository;

import com.concertcomparison.domain.model.User;
import com.concertcomparison.domain.model.UserRole;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface für User Aggregate Root.
 * 
 * DDD Port (Interface im Domain Layer, Implementierung in Infrastructure).
 * Definiert alle Datenbank-Operationen für Users.
 */
public interface UserRepository {
    
    /**
     * Speichert einen User (Create oder Update).
     * 
     * @param user Zu speichernder User
     * @return Gespeicherter User mit generierter ID
     */
    User save(User user);
    
    /**
     * Sucht einen User anhand der ID.
     * 
     * @param id User-ID
     * @return Optional mit User, oder empty wenn nicht gefunden
     */
    Optional<User> findById(Long id);
    
    /**
     * Sucht einen User anhand der Email.
     * 
     * Business Rule: Email ist eindeutig und dient als Username.
     * 
     * @param email Email-Adresse
     * @return Optional mit User, oder empty wenn nicht gefunden
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Liefert alle Users.
     * 
     * @return Liste aller Users
     */
    List<User> findAll();
    
    /**
     * Sucht alle Users nach Rolle.
     * 
     * @param role UserRole (USER oder ADMIN)
     * @return Liste der Users mit der Rolle
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Sucht alle aktiven Users.
     * 
     * @return Liste der Users mit enabled=true
     */
    List<User> findByEnabledTrue();
    
    /**
     * Sucht alle deaktivierten Users.
     * 
     * @return Liste der Users mit enabled=false
     */
    List<User> findByEnabledFalse();
    
    /**
     * Sucht Users nach Vorname oder Nachname (Teilstring-Suche, case-insensitive).
     * 
     * @param firstName Vorname oder Teilstring
     * @param lastName Nachname oder Teilstring
     * @return Liste der gefundenen Users
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    /**
     * Löscht einen User anhand der ID.
     * 
     * ACHTUNG: Normalerweise sollten Users nicht gelöscht, sondern deaktiviert werden.
     * 
     * @param id User-ID
     */
    void deleteById(Long id);
    
    /**
     * Prüft ob ein User mit der ID existiert.
     * 
     * @param id User-ID
     * @return true wenn User existiert
     */
    boolean existsById(Long id);
    
    /**
     * Prüft ob ein User mit der Email existiert.
     * 
     * @param email Email-Adresse
     * @return true wenn User existiert
     */
    boolean existsByEmail(String email);
    
    /**
     * Löscht alle Users.
     * 
     * ACHTUNG: Nur für Tests verwenden!
     */
    void deleteAll();
}

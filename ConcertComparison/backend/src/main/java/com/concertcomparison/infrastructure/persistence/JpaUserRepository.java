package com.concertcomparison.infrastructure.persistence;

import com.concertcomparison.domain.model.User;
import com.concertcomparison.domain.model.UserRole;
import com.concertcomparison.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Implementierung des UserRepository.
 * 
 * Infrastructure Layer: Implementiert das Domain Repository Interface.
 * Verwendet Spring Data JPA für automatische Query-Generierung.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
    
    // Spring Data JPA generiert automatisch Queries basierend auf Methodennamen
    
    @Override
    Optional<User> findByEmail(String email);
    
    @Override
    List<User> findByRole(UserRole role);
    
    @Override
    List<User> findByEnabledTrue();
    
    @Override
    List<User> findByEnabledFalse();
    
    @Override
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    @Override
    boolean existsByEmail(String email);
}

package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.Branch;
import com.rentacar.domain.repository.BranchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-Implementierung des BranchRepository.
 * 
 * Spring Data JPA leitet die Implementierung automatisch ab.
 */
@Repository
public interface JpaBranchRepository extends BranchRepository, JpaRepository<Branch, Long> {
    
    // Alle Methoden werden automatisch von Spring Data JPA implementiert
    // basierend auf den Methodennamen und den Interfaces BranchRepository und JpaRepository
}

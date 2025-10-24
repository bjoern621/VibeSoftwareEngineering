package com.mymensa2.backend.staff.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    
    // Findet alle Mitarbeiter nach Rolle
    @Query("SELECT s FROM Staff s WHERE s.role = :role")
    List<Staff> findByRole(String role);
    
    // Findet alle verfügbaren Mitarbeiter
    @Query("SELECT s FROM Staff s WHERE s.isAvailable = true")
    List<Staff> findAvailable();
    
    // Findet verfügbare Mitarbeiter nach Rolle
    @Query("SELECT s FROM Staff s WHERE s.role = :role AND s.isAvailable = true")
    List<Staff> findByRoleAndAvailable(String role);
}

package com.travelreimburse.infrastructure.persistence;

import com.travelreimburse.domain.model.Employee;
import com.travelreimburse.domain.model.EmployeeRole;
import com.travelreimburse.domain.repository.EmployeeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA-Repository für Employee (Infrastructure Layer)
 * 
 * Erbt von EmployeeRepository (Domain Layer) und JpaRepository (Spring Data)
 * Spring Data JPA generiert automatisch Implementierungen für Standard-CRUD
 * und Query-Methoden
 */
@Repository
public interface JpaEmployeeRepository extends EmployeeRepository, JpaRepository<Employee, Long> {
    
    /**
     * Findet Employee per E-Mail (case-insensitive)
     * Spring Data Query Method - automatisch generiert
     */
    @Override
    Optional<Employee> findByEmail(String email);
    
    /**
     * Findet alle Employees mit einer bestimmten Rolle
     * Spring Data Query Method
     */
    @Override
    List<Employee> findByRole(EmployeeRole role);
    
    /**
     * Findet alle aktiven Employees
     * Custom Query mit @Query Annotation
     */
    @Override
    @Query("SELECT e FROM Employee e WHERE e.active = true")
    List<Employee> findActiveEmployees();
    
    /**
     * Findet alle Employees die einem Manager unterstellt sind
     * Spring Data Query Method
     */
    @Override
    List<Employee> findByManagerId(Long managerId);
    
    /**
     * Prüft ob E-Mail existiert
     * Spring Data Query Method
     */
    @Override
    boolean existsByEmail(String email);
}

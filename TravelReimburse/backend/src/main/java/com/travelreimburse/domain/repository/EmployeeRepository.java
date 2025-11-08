package com.travelreimburse.domain.repository;

import com.travelreimburse.domain.model.Employee;
import com.travelreimburse.domain.model.EmployeeRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository-Interface für Employee (Domain Layer)
 * Abstrakte Definition der Persistierung-Operationen
 * 
 * Implementierung erfolgt in infrastructure/persistence Layer
 */
public interface EmployeeRepository {
    
    /**
     * Speichert einen Employee (Create oder Update)
     */
    Employee save(Employee employee);
    
    /**
     * Findet einen Employee anhand seiner ID
     */
    Optional<Employee> findById(Long id);
    
    /**
     * Findet einen Employee anhand seiner E-Mail-Adresse
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Findet alle Employees mit einer bestimmten Rolle
     */
    List<Employee> findByRole(EmployeeRole role);
    
    /**
     * Findet alle aktiven Employees
     */
    List<Employee> findActiveEmployees();
    
    /**
     * Findet alle Employees die einem Manager unterstellt sind
     */
    List<Employee> findByManagerId(Long managerId);
    
    /**
     * Findet alle Employees (aktive und inaktive)
     */
    List<Employee> findAll();
    
    /**
     * Löscht einen Employee
     */
    void delete(Employee employee);
    
    /**
     * Prüft ob eine E-Mail-Adresse bereits existiert
     */
    boolean existsByEmail(String email);
}

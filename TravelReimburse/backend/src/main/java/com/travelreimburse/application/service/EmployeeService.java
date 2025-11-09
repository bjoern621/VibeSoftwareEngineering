package com.travelreimburse.application.service;

import com.travelreimburse.application.dto.CreateEmployeeDTO;
import com.travelreimburse.application.dto.EmployeeResponseDTO;
import com.travelreimburse.application.dto.UpdateEmployeeDTO;
import com.travelreimburse.domain.exception.EmployeeNotFoundException;
import com.travelreimburse.domain.model.Employee;
import com.travelreimburse.domain.model.EmployeeRole;
import com.travelreimburse.domain.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service für Employee Use Cases
 * Orchestriert CRUD-Operationen für Employees
 * 
 * DDD: Service ist Orchestrator - ruft Entity-Methoden auf
 */
@Service
@Transactional(readOnly = true)
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    /**
     * Erstellt einen neuen Employee
     * @param dto die Daten für den neuen Employee
     * @return der erstellte Employee als DTO
     */
    @Transactional
    public EmployeeResponseDTO createEmployee(CreateEmployeeDTO dto) {
        // Validierung: E-Mail bereits vorhanden?
        if (employeeRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-Mail-Adresse bereits vergeben");
        }
        
        // DTO zu Domain-Objekten konvertieren
        EmployeeRole role = parseRole(dto.role());
        
        // Validierung: Manager existiert?
        if (dto.managerId() != null) {
            employeeRepository.findById(dto.managerId())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.managerId()));
        }
        
        // Domain-Entity erstellen
        Employee employee = new Employee(
            dto.firstName(),
            dto.lastName(),
            dto.email(),
            role
        );
        
        // Optionale Felder setzen
        if (dto.managerId() != null) {
            employee.assignManager(dto.managerId());
        }
        if (dto.departmentCode() != null) {
            employee.assignDepartment(dto.departmentCode());
        }
        if (dto.location() != null) {
            employee.assignLocation(dto.location());
        }
        
        // Persistieren
        Employee saved = employeeRepository.save(employee);
        
        return toResponseDTO(saved);
    }
    
    /**
     * Findet einen Employee anhand seiner ID
     * @param id die ID des Employees
     * @return der Employee als DTO
     */
    public EmployeeResponseDTO findById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        return toResponseDTO(employee);
    }
    
    /**
     * Findet einen Employee anhand seiner E-Mail
     * @param email die E-Mail des Employees
     * @return der Employee als DTO
     */
    public EmployeeResponseDTO findByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EmployeeNotFoundException(email));
        return toResponseDTO(employee);
    }
    
    /**
     * Findet alle Employees mit einer bestimmten Rolle
     * @param roleString die Rolle als String
     * @return Liste aller Employees mit dieser Rolle
     */
    public List<EmployeeResponseDTO> findByRole(String roleString) {
        EmployeeRole role = parseRole(roleString);
        return employeeRepository.findByRole(role)
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    /**
     * Findet alle aktiven Employees
     * @return Liste aller aktiven Employees
     */
    public List<EmployeeResponseDTO> findActiveEmployees() {
        return employeeRepository.findActiveEmployees()
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    /**
     * Findet alle Employees die einem Manager unterstellt sind
     * @param managerId die ID des Managers
     * @return Liste aller Untergebenen
     */
    public List<EmployeeResponseDTO> findByManagerId(Long managerId) {
        return employeeRepository.findByManagerId(managerId)
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    /**
     * Findet alle Employees
     * @return Liste aller Employees
     */
    public List<EmployeeResponseDTO> findAll() {
        return employeeRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }
    
    /**
     * Aktualisiert einen Employee
     * @param id die ID des Employees
     * @param dto die neuen Daten
     * @return der aktualisierte Employee als DTO
     */
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, UpdateEmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        // E-Mail aktualisieren (mit Validierung)
        if (dto.email() != null && !dto.email().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("E-Mail-Adresse bereits vergeben");
            }
            employee.updateEmail(dto.email());
        }
        
        // Rolle ändern
        if (dto.role() != null) {
            EmployeeRole newRole = parseRole(dto.role());
            if (newRole != employee.getRole()) {
                employee.changeRole(newRole);
            }
        }
        
        // Manager zuweisen
        if (dto.managerId() != null && !dto.managerId().equals(employee.getManagerId())) {
            // Validierung: Manager existiert?
            employeeRepository.findById(dto.managerId())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.managerId()));
            employee.assignManager(dto.managerId());
        }
        
        // Abteilung zuweisen
        if (dto.departmentCode() != null) {
            employee.assignDepartment(dto.departmentCode());
        }
        
        // Standort zuweisen
        if (dto.location() != null) {
            employee.assignLocation(dto.location());
        }
        
        // Persistieren
        Employee saved = employeeRepository.save(employee);
        
        return toResponseDTO(saved);
    }
    
    /**
     * Deaktiviert einen Employee
     * @param id die ID des Employees
     */
    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        // Business-Logik aufrufen
        employee.deactivate();
        
        employeeRepository.save(employee);
    }
    
    /**
     * Aktiviert einen Employee
     * @param id die ID des Employees
     */
    @Transactional
    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        // Business-Logik aufrufen
        employee.activate();
        
        employeeRepository.save(employee);
    }
    
    /**
     * Löscht einen Employee (Hard Delete)
     * @param id die ID des Employees
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        employeeRepository.delete(employee);
    }
    
    // ===== Private Helper-Methoden =====
    
    /**
     * Konvertiert Employee-Entity zu DTO
     */
    private EmployeeResponseDTO toResponseDTO(Employee entity) {
        return new EmployeeResponseDTO(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getEmail(),
            entity.getRole().name(),
            entity.getManagerId(),
            entity.getDepartmentCode(),
            entity.getLocation(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }
    
    /**
     * Parsed Rolle aus String
     * @throws IllegalArgumentException wenn Rolle ungültig
     */
    private EmployeeRole parseRole(String roleString) {
        try {
            return EmployeeRole.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Ungültige Rolle: " + roleString + ". Erlaubt: EMPLOYEE, MANAGER, HR, ASSISTANT, FINANCE"
            );
        }
    }
}

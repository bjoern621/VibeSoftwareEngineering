package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.CreateEmployeeDTO;
import com.travelreimburse.application.dto.EmployeeResponseDTO;
import com.travelreimburse.application.dto.UpdateEmployeeDTO;
import com.travelreimburse.application.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für Employee-Verwaltung
 * Nur für Testing/Development - später mit Security absichern!
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    /**
     * GET /api/employees
     * Findet alle Employees oder filtert nach Rolle/Manager
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) Boolean activeOnly) {
        
        List<EmployeeResponseDTO> employees;
        
        if (role != null) {
            employees = employeeService.findByRole(role);
        } else if (managerId != null) {
            employees = employeeService.findByManagerId(managerId);
        } else if (activeOnly != null && activeOnly) {
            employees = employeeService.findActiveEmployees();
        } else {
            employees = employeeService.findAll();
        }
        
        return ResponseEntity.ok(employees);
    }
    
    /**
     * GET /api/employees/{id}
     * Findet einen Employee anhand seiner ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.findById(id);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * GET /api/employees/by-email
     * Findet einen Employee anhand seiner E-Mail
     */
    @GetMapping("/by-email")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmail(@RequestParam String email) {
        EmployeeResponseDTO employee = employeeService.findByEmail(email);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * POST /api/employees
     * Erstellt einen neuen Employee
     */
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody CreateEmployeeDTO dto) {
        EmployeeResponseDTO created = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/employees/{id}
     * Aktualisiert einen Employee
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeDTO dto) {
        EmployeeResponseDTO updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * POST /api/employees/{id}/deactivate
     * Deaktiviert einen Employee
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * POST /api/employees/{id}/activate
     * Aktiviert einen Employee
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateEmployee(@PathVariable Long id) {
        employeeService.activateEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE /api/employees/{id}
     * Löscht einen Employee (Hard Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}

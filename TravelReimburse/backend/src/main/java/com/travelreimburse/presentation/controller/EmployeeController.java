package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.dto.CreateEmployeeDTO;
import com.travelreimburse.application.dto.EmployeeResponseDTO;
import com.travelreimburse.application.dto.UpdateEmployeeDTO;
import com.travelreimburse.application.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für Employee-Verwaltung
 * 
 * Verwaltet Mitarbeiter-Stammdaten inkl. Rollen (EMPLOYEE, MANAGER, HR, ASSISTANT, FINANCE).
 * Unterstützt CRUD-Operationen, Filterung nach Rolle/Manager und Aktivierung/Deaktivierung.
 * 
 * WICHTIG: Derzeit ohne Security - nur für Testing/Development!
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "Verwaltung von Mitarbeiter-Stammdaten und Rollen")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @Operation(
        summary = "Alle Employees abrufen",
        description = "Gibt alle Employees zurück oder filtert nach Rolle, Manager oder aktivem Status. " +
                     "Ohne Parameter werden alle Employees (aktiv + inaktiv) zurückgegeben."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen"),
        @ApiResponse(responseCode = "400", description = "Ungültige Rolle angegeben", 
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployees(
            @Parameter(description = "Filter nach Rolle (EMPLOYEE, MANAGER, HR, ASSISTANT, FINANCE)")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter nach Manager-ID (zeigt alle Untergebenen)")
            @RequestParam(required = false) Long managerId,
            @Parameter(description = "Nur aktive Employees anzeigen (true/false)")
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
    
    @Operation(
        summary = "Employee per ID abrufen",
        description = "Gibt einen einzelnen Employee anhand seiner eindeutigen ID zurück."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee gefunden"),
        @ApiResponse(responseCode = "404", description = "Employee nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @Parameter(description = "ID des Employees", required = true)
            @PathVariable Long id) {
        EmployeeResponseDTO employee = employeeService.findById(id);
        return ResponseEntity.ok(employee);
    }
    
    @Operation(
        summary = "Employee per E-Mail abrufen",
        description = "Sucht einen Employee anhand seiner eindeutigen E-Mail-Adresse."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee gefunden"),
        @ApiResponse(responseCode = "404", description = "Employee mit dieser E-Mail nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @GetMapping("/by-email")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByEmail(
            @Parameter(description = "E-Mail-Adresse des Employees", required = true, example = "max.mustermann@company.com")
            @RequestParam String email) {
        EmployeeResponseDTO employee = employeeService.findByEmail(email);
        return ResponseEntity.ok(employee);
    }
    
    @Operation(
        summary = "Neuen Employee erstellen",
        description = "Erstellt einen neuen Mitarbeiter mit den angegebenen Daten. " +
                     "E-Mail muss eindeutig sein. Manager (falls angegeben) muss existieren."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee erfolgreich erstellt"),
        @ApiResponse(responseCode = "400", description = "Ungültige Daten oder E-Mail bereits vorhanden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Angegebener Manager nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Daten für neuen Employee",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateEmployeeDTO.class))
            )
            @Valid @RequestBody CreateEmployeeDTO dto) {
        EmployeeResponseDTO created = employeeService.createEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Operation(
        summary = "Employee aktualisieren",
        description = "Aktualisiert E-Mail, Rolle oder Manager eines bestehenden Employees. " +
                     "Alle Felder sind optional. Neue E-Mail muss eindeutig sein."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee erfolgreich aktualisiert"),
        @ApiResponse(responseCode = "400", description = "Ungültige Daten oder E-Mail bereits vergeben",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee oder angegebener Manager nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @Parameter(description = "ID des zu aktualisierenden Employees", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Zu aktualisierende Felder (alle optional)",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateEmployeeDTO.class))
            )
            @Valid @RequestBody UpdateEmployeeDTO dto) {
        EmployeeResponseDTO updated = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @Operation(
        summary = "Employee deaktivieren",
        description = "Deaktiviert einen Employee (setzt active=false). " +
                     "Deaktivierte Employees können keine Aktionen mehr ausführen und werden bei activeOnly-Filtern ausgeschlossen."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee erfolgreich deaktiviert"),
        @ApiResponse(responseCode = "400", description = "Employee ist bereits deaktiviert",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateEmployee(
            @Parameter(description = "ID des zu deaktivierenden Employees", required = true)
            @PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Employee aktivieren",
        description = "Aktiviert einen zuvor deaktivierten Employee (setzt active=true). " +
                     "Der Employee kann danach wieder Aktionen ausführen."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee erfolgreich aktiviert"),
        @ApiResponse(responseCode = "400", description = "Employee ist bereits aktiv",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Employee nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateEmployee(
            @Parameter(description = "ID des zu aktivierenden Employees", required = true)
            @PathVariable Long id) {
        employeeService.activateEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Employee löschen (Hard Delete)",
        description = "Löscht einen Employee permanent aus der Datenbank. " +
                     "ACHTUNG: Dies ist ein Hard Delete und kann nicht rückgängig gemacht werden! " +
                     "In Produktion sollte stattdessen Deaktivierung verwendet werden."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee erfolgreich gelöscht"),
        @ApiResponse(responseCode = "404", description = "Employee nicht gefunden",
                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "ID des zu löschenden Employees", required = true)
            @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}

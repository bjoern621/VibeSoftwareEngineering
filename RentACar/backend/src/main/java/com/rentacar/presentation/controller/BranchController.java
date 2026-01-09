package com.rentacar.presentation.controller;

import com.rentacar.application.service.BranchApplicationService;
import com.rentacar.presentation.dto.BranchResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller für Branch (Filiale) Verwaltung.
 * 
 * Stellt Endpoints zum Abrufen von Filialinformationen bereit.
 */
@RestController
@RequestMapping("/api/branches")
public class BranchController {
    
    private final BranchApplicationService branchApplicationService;
    
    public BranchController(BranchApplicationService branchApplicationService) {
        this.branchApplicationService = branchApplicationService;
    }
    
    /**
     * GET /api/branches
     * 
     * Gibt alle verfügbaren Filialen zurück.
     * 
     * @return Liste aller Filialen
     */
    @GetMapping
    public ResponseEntity<List<BranchResponseDTO>> getAllBranches() {
        List<BranchResponseDTO> branches = branchApplicationService
            .getAllBranches()
            .stream()
            .map(BranchResponseDTO::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(branches);
    }
}

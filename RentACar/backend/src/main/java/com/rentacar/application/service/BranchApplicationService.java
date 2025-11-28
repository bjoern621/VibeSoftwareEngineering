package com.rentacar.application.service;

import com.rentacar.domain.model.Branch;
import com.rentacar.domain.repository.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Service für Branch (Filiale) Use Cases.
 */
@Service
@Transactional
public class BranchApplicationService {
    
    private final BranchRepository branchRepository;
    
    public BranchApplicationService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }
    
    /**
     * Gibt alle verfügbaren Filialen zurück.
     * 
     * @return Liste aller Filialen
     */
    @Transactional(readOnly = true)
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
}

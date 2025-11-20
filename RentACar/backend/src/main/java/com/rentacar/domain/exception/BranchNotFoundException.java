package com.rentacar.domain.exception;

/**
 * Exception für den Fall, dass eine Filiale nicht gefunden wurde.
 * 
 * Domain-spezifische Exception gemäß DDD-Prinzipien.
 */
public class BranchNotFoundException extends RuntimeException {
    
    private final Long branchId;
    
    /**
     * Erstellt eine neue BranchNotFoundException.
     * 
     * @param branchId die ID der nicht gefundenen Filiale
     */
    public BranchNotFoundException(Long branchId) {
        super("Filiale mit ID " + branchId + " wurde nicht gefunden");
        this.branchId = branchId;
    }
    
    public Long getBranchId() {
        return branchId;
    }
}

package com.travelreimburse.domain.exception;

import com.travelreimburse.domain.model.AbsenceInfo;
import java.util.List;

/**
 * Exception wenn ein Reiseantrag mit bestehenden Abwesenheiten kollidiert
 */
public class AbsenceConflictException extends RuntimeException {
    
    private final List<AbsenceInfo> conflicts;

    public AbsenceConflictException(List<AbsenceInfo> conflicts) {
        super("Reise kollidiert mit " + conflicts.size() + " bestehenden Abwesenheit(en)");
        this.conflicts = List.copyOf(conflicts); // Defensive copy
    }

    public List<AbsenceInfo> getConflicts() {
        return conflicts;
    }
}

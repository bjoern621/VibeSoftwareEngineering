package com.travelreimburse.domain.exception;

/**
 * Exception wenn ein Mitarbeiter nicht die erforderliche Berechtigung hat
 */
public class InsufficientPermissionException extends RuntimeException {
    
    private final Long employeeId;
    private final String action;
    
    public InsufficientPermissionException(Long employeeId, String action) {
        super(String.format("Mitarbeiter mit ID %d hat nicht die erforderliche Berechtigung für: %s", 
                          employeeId, action));
        this.employeeId = employeeId;
        this.action = action;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public String getAction() {
        return action;
    }
}

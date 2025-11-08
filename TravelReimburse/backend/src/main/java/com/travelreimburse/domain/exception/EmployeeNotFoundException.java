package com.travelreimburse.domain.exception;

/**
 * Exception wenn ein Employee nicht gefunden wird
 */
public class EmployeeNotFoundException extends RuntimeException {
    
    public EmployeeNotFoundException(Long id) {
        super("Employee mit ID " + id + " nicht gefunden");
    }
    
    public EmployeeNotFoundException(String email) {
        super("Employee mit E-Mail " + email + " nicht gefunden");
    }
    
    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

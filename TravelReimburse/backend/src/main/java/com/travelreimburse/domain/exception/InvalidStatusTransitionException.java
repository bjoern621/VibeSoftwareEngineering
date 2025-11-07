package com.travelreimburse.domain.exception;

/**
 * Domain exception thrown when attempting an invalid status transition.
 * Represents a violation of business rules in the domain model.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}


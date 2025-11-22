package com.rentacar.domain.exception;

import java.time.LocalDateTime;

/**
 * Exception für ungültige Zeiträume.
 * 
 * Wird geworfen, wenn ein DateRange ungültig ist (z.B. Startdatum nach Enddatum).
 */
public class InvalidDateRangeException extends RuntimeException {

    private final LocalDateTime start;
    private final LocalDateTime end;

    public InvalidDateRangeException(String message) {
        super(message);
        this.start = null;
        this.end = null;
    }

    public InvalidDateRangeException(LocalDateTime start, LocalDateTime end, String message) {
        super(message);
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}

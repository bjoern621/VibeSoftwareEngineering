package com.rentacar.domain.exception;

/**
 * Exception, die geworfen wird, wenn ein Benutzer zu viele fehlgeschlagene Login-Versuche hatte.
 *
 * Diese Exception signalisiert, dass das Rate Limit für Login-Versuche überschritten wurde
 * und der Account temporär gesperrt ist.
 */
public class TooManyLoginAttemptsException extends RuntimeException {

    private final String username;
    private final long retryAfterSeconds;

    /**
     * Erstellt eine neue TooManyLoginAttemptsException.
     *
     * @param username der betroffene Benutzername
     * @param retryAfterSeconds Anzahl der Sekunden bis zum nächsten Versuch
     */
    public TooManyLoginAttemptsException(String username, long retryAfterSeconds) {
        super(String.format(
            "Zu viele fehlgeschlagene Login-Versuche für Benutzer '%s'. Bitte versuchen Sie es in %d Sekunden erneut.",
            username,
            retryAfterSeconds
        ));
        this.username = username;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getUsername() {
        return username;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}


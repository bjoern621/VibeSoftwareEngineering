package com.concertcomparison.domain.exception;

/**
 * Wird geworfen, wenn die Rate Limit überschritten wird.
 * Diese Domain-spezifische Exception wird vom Rate Limit Filter ausgelöst.
 */
public class RateLimitExceededException extends RuntimeException {

    private final String clientIdentifier;
    private final long retryAfterSeconds;

    /**
     * Erstellt eine neue RateLimitExceededException.
     *
     * @param message             Die Fehlermeldung
     * @param clientIdentifier    Die Identifikation des Clients (IP oder User ID)
     * @param retryAfterSeconds   Die Anzahl der Sekunden bis zum nächsten Versuch
     */
    public RateLimitExceededException(String message, String clientIdentifier, long retryAfterSeconds) {
        super(message);
        this.clientIdentifier = clientIdentifier;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Erstellt eine neue RateLimitExceededException mit Ursache.
     *
     * @param message             Die Fehlermeldung
     * @param clientIdentifier    Die Identifikation des Clients (IP oder User ID)
     * @param retryAfterSeconds   Die Anzahl der Sekunden bis zum nächsten Versuch
     * @param cause               Die ursprüngliche Exception
     */
    public RateLimitExceededException(String message, String clientIdentifier, long retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.clientIdentifier = clientIdentifier;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

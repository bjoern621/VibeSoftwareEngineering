package com.rentacar.infrastructure.security;

import com.rentacar.domain.exception.TooManyLoginAttemptsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service für Rate Limiting von Login-Versuchen.
 *
 * Implementiert Bucket4j-basiertes Rate Limiting um Brute-Force-Attacken zu verhindern.
 * Pro Benutzername sind maximal 5 fehlgeschlagene Login-Versuche in 15 Minuten erlaubt.
 *
 * Nach Überschreitung wird der Account für 15 Minuten gesperrt.
 * Erfolgreiche Logins setzen den Counter zurück.
 */
@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(15);

    // Thread-safe Map zur Speicherung der Buckets pro Username
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Erstellt oder holt einen Bucket für einen Benutzernamen.
     *
     * Konfiguration:
     * - Capacity: 5 Tokens (= 5 Versuche)
     * - Refill: 5 Tokens alle 15 Minuten
     *
     * @param username der Benutzername
     * @return Bucket für diesen Benutzer
     */
    private Bucket resolveBucket(String username) {
        return buckets.computeIfAbsent(username, key -> createNewBucket());
    }

    /**
     * Erstellt einen neuen Bucket mit der konfigurierten Rate-Limit-Policy.
     *
     * @return neuer Bucket
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(MAX_ATTEMPTS)
            .refillIntervally(MAX_ATTEMPTS, WINDOW_DURATION)
            .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Prüft, ob ein Login-Versuch erlaubt ist und verbraucht einen Token.
     *
     * @param username der Benutzername
     * @throws TooManyLoginAttemptsException wenn Rate Limit überschritten
     */
    public void checkLoginAttempt(String username) {
        Bucket bucket = resolveBucket(username);

        if (!bucket.tryConsume(1)) {
            long secondsUntilRefill = WINDOW_DURATION.getSeconds();
            throw new TooManyLoginAttemptsException(username, secondsUntilRefill);
        }
    }

    /**
     * Setzt den Login-Counter für einen Benutzer zurück.
     *
     * Wird bei erfolgreichem Login aufgerufen.
     *
     * @param username der Benutzername
     */
    public void resetLoginAttempts(String username) {
        buckets.remove(username);
    }

    /**
     * Gibt die verbleibenden Login-Versuche für einen Benutzer zurück.
     *
     * @param username der Benutzername
     * @return Anzahl verbleibender Versuche
     */
    public long getRemainingAttempts(String username) {
        Bucket bucket = resolveBucket(username);
        return bucket.getAvailableTokens();
    }

    /**
     * Prüft, ob ein Benutzer aktuell gesperrt ist.
     *
     * @param username der Benutzername
     * @return true wenn gesperrt, false sonst
     */
    public boolean isBlocked(String username) {
        Bucket bucket = resolveBucket(username);
        return bucket.getAvailableTokens() == 0;
    }
}


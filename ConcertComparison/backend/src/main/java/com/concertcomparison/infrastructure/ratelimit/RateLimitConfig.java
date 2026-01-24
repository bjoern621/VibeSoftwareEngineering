package com.concertcomparison.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Konfiguration für Rate Limiting mit Bucket4j.
 * Verwaltet die Erstellung und das Caching von Token Buckets pro Client-Identifikator.
 */
@Configuration
@EnableConfigurationProperties(RateLimitConfig.RateLimitProperties.class)
public class RateLimitConfig {

    /**
     * Rate Limit Properties aus application.properties.
     */
    @ConfigurationProperties(prefix = "ratelimit")
    public static class RateLimitProperties {
        private boolean enabled = true;
        private int requestsPerMinute = 100;
        private int requestsPerMinuteHold = 10;
        private int requestsPerMinuteCheckout = 30;
        private List<String> whitelistIps = new ArrayList<>();
        private List<String> whitelistRoles = new ArrayList<>();
        private Map<String, Integer> endpointLimits = new HashMap<>();

        // Getter and Setter
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getRequestsPerMinuteHold() {
            return requestsPerMinuteHold;
        }

        public void setRequestsPerMinuteHold(int requestsPerMinuteHold) {
            this.requestsPerMinuteHold = requestsPerMinuteHold;
        }

        public int getRequestsPerMinuteCheckout() {
            return requestsPerMinuteCheckout;
        }

        public void setRequestsPerMinuteCheckout(int requestsPerMinuteCheckout) {
            this.requestsPerMinuteCheckout = requestsPerMinuteCheckout;
        }

        public List<String> getWhitelistIps() {
            return whitelistIps;
        }

        public void setWhitelistIps(List<String> whitelistIps) {
            this.whitelistIps = whitelistIps;
        }

        public List<String> getWhitelistRoles() {
            return whitelistRoles;
        }

        public void setWhitelistRoles(List<String> whitelistRoles) {
            this.whitelistRoles = whitelistRoles;
        }

        public Map<String, Integer> getEndpointLimits() {
            return endpointLimits;
        }

        public void setEndpointLimits(Map<String, Integer> endpointLimits) {
            this.endpointLimits = endpointLimits;
        }
    }

    /**
     * Service zur Verwaltung von Buckets und Rate Limit Überprüfungen.
     */
    @Service
    public static class RateLimitService {

        private final RateLimitProperties properties;
        private final Map<String, Bucket> buckets = new HashMap<>();

        public RateLimitService(RateLimitProperties properties) {
            this.properties = properties;
        }

        /**
         * Gibt oder erstellt einen Bucket für den gegebenen Client-Identifikator.
         * Der Bucket wird mit Token Refill alle 60 Sekunden aufgefüllt.
         *
         * @param clientId Die Client-Identifikation (IP oder User ID)
         * @param endpoint Der API-Endpoint (für individuelle Limits)
         * @return Der Bucket für den Client
         */
        public Bucket resolveBucket(String clientId, String endpoint) {
            String bucketKey = clientId + ":" + endpoint;

            return buckets.computeIfAbsent(bucketKey, key -> createBucket(endpoint));
        }

        /**
         * Normalisiert einen Endpoint durch Entfernen aller Slashes.
         * YAML entfernt Slashes aus Map-Keys, daher müssen wir sie auch im Request-Pfad entfernen.
         *
         * @param endpoint Der Original-Endpoint (z.B. /api/auth/login)
         * @return Der normalisierte Endpoint (z.B. apiauthlogin)
         */
        private String normalizeEndpoint(String endpoint) {
            return endpoint.replaceAll("/", "");
        }

        /**
         * Erstellt einen neuen Token Bucket basierend auf dem Endpoint und dem globalen Limit.
         *
         * @param endpoint Der API-Endpoint
         * @return Der neu erstellte Bucket
         */
        private Bucket createBucket(String endpoint) {
            // Prüfe auf endpoint-spezifisches Limit
            // YAML entfernt Slashes aus Keys, daher normalisieren wir den Endpoint
            String normalizedEndpoint = normalizeEndpoint(endpoint);
            int limit = properties.getEndpointLimits().getOrDefault(normalizedEndpoint, properties.getRequestsPerMinute());

            Bandwidth bandwidth = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
            return Bucket4j.builder()
                    .addLimit(bandwidth)
                    .build();
        }

        /**
         * Überprüft, ob der Client das Rate Limit überschritten hat.
         *
         * @param clientId Der Client-Identifikator
         * @param endpoint Der API-Endpoint
         * @return true, wenn ein Token verbraucht werden konnte; false, wenn das Limit überschritten ist
         */
        public boolean allowRequest(String clientId, String endpoint) {
            Bucket bucket = resolveBucket(clientId, endpoint);
            return bucket.tryConsume(1);
        }

        /**
         * Gibt die Anzahl der verfügbaren Tokens für den Client.
         *
         * @param clientId Der Client-Identifikator
         * @param endpoint Der API-Endpoint
         * @return Die Anzahl der verfügbaren Tokens
         */
        public long getAvailableTokens(String clientId, String endpoint) {
            Bucket bucket = resolveBucket(clientId, endpoint);
            return bucket.getAvailableTokens();
        }

        /**
         * Berechnet die Sekunden bis zum nächsten verfügbaren Token.
         * Nutzt Bucket4j ConsumptionProbe, um die Wartezeit präzise zu ermitteln.
         *
         * @param clientId Der Client-Identifikator
         * @param endpoint Der API-Endpoint
         * @return Die Anzahl der Sekunden bis zum nächsten Token
         */
        public long getRetryAfterSeconds(String clientId, String endpoint) {
            Bucket bucket = resolveBucket(clientId, endpoint);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                return 0;
            }
            long nanos = probe.getNanosToWaitForRefill();
            long seconds = (long) Math.ceil(nanos / 1_000_000_000.0);
            return Math.max(seconds, 1);
        }

        /**
         * Prüft, ob die Client-IP auf der Whitelist steht.
         *
         * @param clientIp Die Client-IP
         * @return true, wenn die IP auf der Whitelist steht
         */
        public boolean isIpWhitelisted(String clientIp) {
            return properties.getWhitelistIps().contains(clientIp);
        }

        /**
         * Prüft, ob die Rolle auf der Whitelist steht.
         *
         * @param role Die Rolle des Users
         * @return true, wenn die Rolle auf der Whitelist steht
         */
        public boolean isRoleWhitelisted(String role) {
            return properties.getWhitelistRoles().contains(role);
        }

        /**
         * Prüft, ob Rate Limiting aktiviert ist.
         *
         * @return true, wenn Rate Limiting aktiviert ist
         */
        public boolean isEnabled() {
            return properties.isEnabled();
        }

        /**
         * Extrahiert den Endpoint-Pfad aus der Request-URI.
         * Beispiel: /api/reservations -> /api/reservations
         *
         * @param requestUri Die Request-URI
         * @return Der Endpoint-Pfad
         */
        public String extractEndpoint(String requestUri) {
            // Entferne Query-Parameter
            String path = requestUri.split("\\?")[0];

            // Entferne Query-Parameter vom Anfang
            if (path.contains("?")) {
                path = path.substring(0, path.indexOf("?"));
            }

            return path;
        }
    }
}

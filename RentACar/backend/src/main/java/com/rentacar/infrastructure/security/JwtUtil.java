package com.rentacar.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility-Klasse für JWT-Token-Generierung und -Validierung.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // in Millisekunden

    /**
     * Generiert einen JWT-Token für einen Benutzer.
     *
     * @param email E-Mail-Adresse des Benutzers
     * @param customerId Kunden-ID
     * @return JWT-Token
     */
    public String generateToken(String email, Long customerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerId", customerId);
        claims.put("role", "CUSTOMER");
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrahiert die E-Mail-Adresse aus dem Token.
     *
     * @param token JWT-Token
     * @return E-Mail-Adresse
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrahiert die Kunden-ID aus dem Token.
     *
     * @param token JWT-Token
     * @return Kunden-ID
     */
    public Long extractCustomerId(String token) {
        return extractClaim(token, claims -> claims.get("customerId", Long.class));
    }

    /**
     * Extrahiert das Ablaufdatum aus dem Token.
     *
     * @param token JWT-Token
     * @return Ablaufdatum
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Prüft, ob der Token abgelaufen ist.
     *
     * @param token JWT-Token
     * @return true wenn abgelaufen
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validiert den Token gegen UserDetails.
     *
     * @param token JWT-Token
     * @param userDetails UserDetails
     * @return true wenn gültig
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extrahiert die Customer-ID aus der aktuellen Authentication.
     * 
     * @param authentication Die aktuelle Spring Security Authentication
     * @return Customer-ID aus dem JWT-Token
     * @throws IllegalStateException wenn keine gültige Authentifizierung vorhanden
     */
    public Long extractCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("Keine gültige Authentifizierung vorhanden");
        }
        
        String token = authentication.getCredentials().toString();
        return extractCustomerId(token);
    }
}

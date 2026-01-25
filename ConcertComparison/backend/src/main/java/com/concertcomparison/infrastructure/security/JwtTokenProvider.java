package com.concertcomparison.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Utility-Klasse.
 * 
 * Verantwortlich für:
 * - JWT Token Generierung
 * - JWT Token Validierung
 * - Token Claims Extraktion
 * 
 * Verwendet JJWT Library mit HS512 Signatur-Algorithmus.
 */
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24 Stunden in Millisekunden
    private Long jwtExpirationMs;
    
    /**
     * Generiert einen JWT Token für den gegebenen User.
     * 
     * @param userDetails Spring Security UserDetails
     * @return JWT Token String
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    /**
     * Erstellt einen JWT Token mit Claims und Subject.
     * 
     * @param claims Zusätzliche Claims
     * @param subject Subject (Username/Email)
     * @return JWT Token String
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extrahiert den Username (Subject) aus dem Token.
     * 
     * @param token JWT Token
     * @return Username/Email
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrahiert das Ablaufdatum aus dem Token.
     * 
     * @param token JWT Token
     * @return Ablaufdatum
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrahiert einen spezifischen Claim aus dem Token.
     * 
     * @param token JWT Token
     * @param claimsResolver Function zum Extrahieren des Claims
     * @return Claim-Wert
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrahiert alle Claims aus dem Token.
     * 
     * @param token JWT Token
     * @return Claims
     */
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
     * @param token JWT Token
     * @return true wenn abgelaufen, false sonst
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Validiert den Token gegen UserDetails.
     * 
     * @param token JWT Token
     * @param userDetails Spring Security UserDetails
     * @return true wenn Token valide, false sonst
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Generiert den SecretKey für Token-Signierung.
     * 
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}

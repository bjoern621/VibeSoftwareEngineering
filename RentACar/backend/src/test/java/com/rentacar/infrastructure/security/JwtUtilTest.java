package com.rentacar.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "mySecretKeyWhichIsVeryLongAndSecureEnoughForTestingPurposes1234567890";
    private Long expiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret, expiration);
    }

    @Test
    void generateToken_And_ExtractClaims() {
        String email = "test@example.com";
        Long customerId = 123L;

        String token = jwtUtil.generateToken(email, customerId);

        assertNotNull(token);
        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(customerId, jwtUtil.extractCustomerId(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void validateToken_Success() {
        String email = "test@example.com";
        Long customerId = 123L;
        String token = jwtUtil.generateToken(email, customerId);

        UserDetails userDetails = new User(email, "password", Collections.emptyList());

        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_Fail_WrongUsername() {
        String email = "test@example.com";
        Long customerId = 123L;
        String token = jwtUtil.generateToken(email, customerId);

        UserDetails userDetails = new User("other@example.com", "password", Collections.emptyList());

        assertFalse(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void isTokenExpired_True() {
        // Create a token with very short expiration
        JwtUtil shortLivedJwtUtil = new JwtUtil(secret, 1L); // 1ms expiration
        String token = shortLivedJwtUtil.generateToken("test@example.com", 123L);

        // Note: isTokenExpired might throw ExpiredJwtException if parsing fails due to expiration, 
        // or return true if it handles it. 
        // Looking at implementation: extractExpiration calls extractClaim calls extractAllClaims calls parser().parseSignedClaims(token)
        // Jwts parser throws ExpiredJwtException if token is expired.
        // So we should expect exception or handle it.
        // The method isTokenExpired calls extractExpiration.
        
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> shortLivedJwtUtil.isTokenExpired(token));
    }

    @Test
    void extractCustomerId_FromAuthentication() {
        String email = "test@example.com";
        Long customerId = 123L;
        String token = jwtUtil.generateToken(email, customerId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(token);

        assertEquals(customerId, jwtUtil.extractCustomerId(authentication));
    }
    
    @Test
    void extractCustomerId_FromAuthentication_Null() {
        assertThrows(IllegalStateException.class, () -> jwtUtil.extractCustomerId((Authentication) null));
    }
}

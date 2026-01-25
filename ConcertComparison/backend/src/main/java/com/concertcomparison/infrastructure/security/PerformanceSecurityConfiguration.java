package com.concertcomparison.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Performance Test Security Configuration.
 * 
 * Diese Konfiguration ist NUR aktiv für das "performance" Profil
 * und deaktiviert alle Authentifizierungs-/Autorisierungs-Prüfungen.
 * 
 * WICHTIG:
 * - Wird ausschließlich für Gatling Load Tests verwendet
 * - Security ist standardmäßig aktiv (siehe {@link SecurityConfiguration})
 * - Diese Konfiguration ersetzt SecurityConfiguration NUR wenn
 *   Spring Boot mit --spring.profiles.active=performance gestartet wird
 * 
 * Warum Security für Performance-Tests deaktivieren?
 * - Gatling testet die Business-Logik unter Last, nicht die Auth-Layer
 * - Reduziert Test-Komplexität (keine Token-Verwaltung nötig)
 * - Praxis-konform: Load Tests fokussieren auf Domain-Performance
 * 
 * @author Concert Comparison Team
 */
@Configuration
@EnableWebSecurity
@Profile("performance")
public class PerformanceSecurityConfiguration {
    
    /**
     * Security Filter Chain für Performance-Tests.
     * 
     * Erlaubt alle Requests ohne Authentifizierung/Autorisierung.
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception bei Konfigurationsfehlern
     */
    @Bean
    public SecurityFilterChain performanceSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF komplett deaktivieren
                .csrf(AbstractHttpConfigurer::disable)
                
                // ALLE Requests erlauben (keine Authentifizierung)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        
        return http.build();
    }
    
    /**
     * Password Encoder Bean.
     * 
     * Wird von AuthService benötigt, auch wenn Security deaktiviert ist.
     * BCrypt mit Strength 10.
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    
    /**
     * Authentication Manager Bean.
     * 
     * Wird von AuthService benötigt, auch wenn Security deaktiviert ist.
     * 
     * @param authConfig AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception bei Konfigurationsfehlern
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

package com.concertcomparison.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration (Development Mode).
 * 
 * WICHTIG: Dies ist eine ENTWICKLUNGS-Konfiguration!
 * In Produktion muss richtige Authentication/Authorization implementiert werden.
 * 
 * TODO: Für Produktion:
 * - JWT-basierte Authentication implementieren
 * - Rollen-basierte Authorization (USER, ADMIN)
 * - CSRF Protection aktivieren
 * - Rate Limiting hinzufügen
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Für Development - IN PRODUKTION AKTIVIEREN!
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/**",           // Alle API Endpoints
                    "/events/**",        // Concert-Liste Endpoint
                    "/actuator/health",  // Health Check (Load Balancer braucht das!)
                    "/h2-console/**",    // H2 Console
                    "/swagger-ui/**",    // Swagger UI
                    "/api-docs/**"       // OpenAPI Docs
                ).permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()) // Für H2 Console
            );
        
        return http.build();
    }
}

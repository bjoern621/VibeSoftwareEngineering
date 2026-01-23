package com.concertcomparison.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Konfiguration mit JWT Authentication.
 * 
 * Konfiguriert:
 * - JWT-basierte Authentifizierung (Stateless Sessions)
 * - BCrypt Password Encoding
 * - Endpoint-Autorisierung (Public vs. Protected)
 * - CORS und CSRF Einstellungen
 * 
 * WICHTIG: Diese Konfiguration ist NICHT aktiv im "performance" Profil.
 * Für Performance-Tests (Gatling) siehe {@link PerformanceSecurityConfiguration}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@org.springframework.context.annotation.Profile("!performance & !test")
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    
    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * Security Filter Chain Konfiguration.
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deaktivieren (JWT ist CSRF-resistent)
                .csrf(AbstractHttpConfigurer::disable)
                
                // Session Management: Stateless (keine Server-Sessions)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints (kein Login erforderlich)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Concerts - Read-Only (GET) für alle
                        .requestMatchers(HttpMethod.GET, "/api/concerts/**").permitAll()
                        
                        // Concerts - Admin-Only (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/concerts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/concerts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/concerts/**").hasRole("ADMIN")
                        
                        // Events & Seats - Read-Only für alle
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seats/**").permitAll()
                        
                        // Seat Hold - User kann Seats reservieren
                        .requestMatchers(HttpMethod.POST, "/api/seats/*/hold").hasAnyRole("USER", "ADMIN")
                        
                        // Admin-Only Endpoints (Events, Seats Bulk)
                        .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/seats/**").hasRole("ADMIN")
                        
                        // User Endpoints (Login erforderlich)
                        .requestMatchers("/api/reservations/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                        
                        // Alle anderen Requests erfordern Authentifizierung
                        .anyRequest().authenticated()
                )
                
                // Authentication Entry Point: Return 401 for unauthenticated requests
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentifizierung erforderlich\"}");
                        })
                )
                
                // Authentication Provider
                .authenticationProvider(authenticationProvider())
                
                // JWT Filter vor UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Password Encoder Bean.
     * 
     * BCrypt mit Strength 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    
    /**
     * Authentication Provider Bean.
     * 
     * Verbindet UserDetailsService mit PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Authentication Manager Bean.
     * 
     * Wird für Login (AuthService) benötigt.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

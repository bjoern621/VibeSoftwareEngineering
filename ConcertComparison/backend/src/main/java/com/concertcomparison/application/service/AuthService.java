package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.User;
import com.concertcomparison.domain.model.UserRole;
import com.concertcomparison.domain.repository.UserRepository;
import com.concertcomparison.infrastructure.security.JwtTokenProvider;
import com.concertcomparison.presentation.dto.LoginRequest;
import com.concertcomparison.presentation.dto.LoginResponse;
import com.concertcomparison.presentation.dto.RegisterRequest;
import com.concertcomparison.presentation.dto.UserProfileResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service für Authentifizierung und User-Management.
 * 
 * Orchestriert:
 * - User-Registrierung mit Password Hashing
 * - Login mit JWT Token Generation
 * - User-Profil Abfrage
 * 
 * Business Rules:
 * - Email muss eindeutig sein
 * - Password wird mit BCrypt gehashed
 * - Default Role: USER
 * - Login generiert JWT Token
 */
@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Registriert einen neuen User.
     * 
     * @param request RegisterRequest
     * @return UserProfileResponse
     * @throws IllegalArgumentException wenn Email bereits existiert
     */
    public UserProfileResponse register(RegisterRequest request) {
        // 1. Prüfen ob Email bereits existiert
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException(
                    "User mit Email existiert bereits: " + request.email());
        }
        
        // 2. Passwort hashen
        String hashedPassword = passwordEncoder.encode(request.password());
        
        // 3. User Entity erstellen
        User user = User.create(
                request.email(),
                hashedPassword,
                request.firstName(),
                request.lastName(),
                UserRole.USER // Default Role
        );
        
        // 4. User speichern
        User savedUser = userRepository.save(user);
        
        // 5. Response DTO erstellen
        return mapToProfileResponse(savedUser);
    }
    
    /**
     * Authentifiziert einen User und generiert JWT Token.
     * 
     * @param request LoginRequest
     * @return LoginResponse mit JWT Token
     * @throws org.springframework.security.core.AuthenticationException bei falschen Credentials
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Authentifizierung durchführen
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        
        // 2. UserDetails aus Authentication extrahieren
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 3. JWT Token generieren
        String token = jwtTokenProvider.generateToken(userDetails);
        
        // 4. User aus DB laden für Role
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException(
                        "User nicht gefunden nach erfolgreicher Authentifizierung"));
        
        // 5. Response erstellen
        return LoginResponse.of(token, user.getEmail(), user.getRole().name());
    }
    
    /**
     * Liefert das Profil des aktuell authentifizierten Users.
     * 
     * @param email Email des Users
     * @return UserProfileResponse
     * @throws IllegalArgumentException wenn User nicht gefunden
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User nicht gefunden: " + email));
        
        return mapToProfileResponse(user);
    }
    
    /**
     * Mapped User Entity zu UserProfileResponse DTO.
     * 
     * @param user User Entity
     * @return UserProfileResponse DTO
     */
    private UserProfileResponse mapToProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getEnabled()
        );
    }
}

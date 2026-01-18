package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.User;
import com.concertcomparison.domain.model.UserRole;
import com.concertcomparison.domain.repository.UserRepository;
import com.concertcomparison.infrastructure.security.JwtTokenProvider;
import com.concertcomparison.presentation.dto.LoginRequest;
import com.concertcomparison.presentation.dto.LoginResponse;
import com.concertcomparison.presentation.dto.RegisterRequest;
import com.concertcomparison.presentation.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für AuthService.
 * 
 * Testet:
 * - User-Registrierung mit Password Hashing
 * - Login mit JWT Token Generation
 * - User-Profil Abfrage
 * - Exception-Handling (Duplicate Email, User Not Found)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "test@example.com",
                "password123",
                "Max",
                "Mustermann"
        );
        
        testUser = User.create(
                "test@example.com",
                "$2a$10$hashedPassword",
                "Max",
                "Mustermann",
                UserRole.USER
        );
    }
    
    // ==================== REGISTRATION TESTS ====================
    
    @Test
    @DisplayName("Register - Erfolgreiche User-Registrierung")
    void register_Success() {
        // Given
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserProfileResponse response = authService.register(registerRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(registerRequest.email());
        assertThat(response.firstName()).isEqualTo(registerRequest.firstName());
        assertThat(response.lastName()).isEqualTo(registerRequest.lastName());
        assertThat(response.role()).isEqualTo("USER");
        
        verify(userRepository).findByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Register - Fehlschlag bei bereits existierender Email")
    void register_EmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.of(testUser));
        
        // When / Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User mit Email existiert bereits");
        
        verify(userRepository).findByEmail(registerRequest.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Register - Password wird korrekt gehashed")
    void register_PasswordIsHashed() {
        // Given
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        authService.register(registerRequest);
        
        // Then
        verify(passwordEncoder).encode(registerRequest.password());
    }
    
    // ==================== LOGIN TESTS ====================
    
    @Test
    @DisplayName("Login - Erfolgreicher Login mit JWT Token")
    void login_Success() {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("jwt-token-123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // When
        LoginResponse response = authService.login(loginRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo("USER");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(userDetails);
    }
    
    @Test
    @DisplayName("Login - Fehlschlag bei falschen Credentials")
    void login_InvalidCredentials() {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongPassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));
        
        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(any());
    }
    
    // ==================== GET PROFILE TESTS ====================
    
    @Test
    @DisplayName("GetProfile - Erfolgreiche Profil-Abfrage")
    void getProfile_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // When
        UserProfileResponse response = authService.getProfile("test@example.com");
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("Max");
        assertThat(response.lastName()).isEqualTo("Mustermann");
        assertThat(response.role()).isEqualTo("USER");
        
        verify(userRepository).findByEmail("test@example.com");
    }
    
    @Test
    @DisplayName("GetProfile - User nicht gefunden")
    void getProfile_UserNotFound() {
        // Given
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        
        // When / Then
        assertThatThrownBy(() -> authService.getProfile("unknown@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User nicht gefunden");
        
        verify(userRepository).findByEmail("unknown@example.com");
    }
}

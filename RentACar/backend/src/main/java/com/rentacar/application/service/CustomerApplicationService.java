package com.rentacar.application.service;

import com.rentacar.domain.exception.DuplicateDriverLicenseException;
import com.rentacar.domain.exception.DuplicateEmailException;
import com.rentacar.domain.exception.CustomerNotFoundException;
import com.rentacar.domain.exception.ExpiredVerificationTokenException;
import com.rentacar.domain.exception.InvalidVerificationTokenException;
import com.rentacar.domain.model.Address;
import com.rentacar.domain.model.Customer;
import com.rentacar.domain.model.DriverLicenseNumber;
import com.rentacar.domain.model.RefreshToken;
import com.rentacar.domain.repository.CustomerRepository;
import com.rentacar.domain.service.EmailService;
import com.rentacar.domain.service.RefreshTokenService;
import com.rentacar.domain.service.TokenBlacklistService;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.infrastructure.security.LoginRateLimiterService;
import com.rentacar.presentation.dto.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Application Service für Kundenverwaltung.
 * Orchestriert Use Cases für Registrierung, Authentifizierung und Profilverwaltung.
 */
@Service
@Transactional
public class CustomerApplicationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final LoginRateLimiterService loginRateLimiterService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    private final boolean autoVerifyEmail;

    public CustomerApplicationService(CustomerRepository customerRepository,
                                     PasswordEncoder passwordEncoder,
                                     AuthenticationManager authenticationManager,
                                     JwtUtil jwtUtil,
                                     EmailService emailService,
                                     LoginRateLimiterService loginRateLimiterService,
                                     TokenBlacklistService tokenBlacklistService,
                                     RefreshTokenService refreshTokenService,
                                     @org.springframework.beans.factory.annotation.Value("${customer.auto-verify-email:false}") boolean autoVerifyEmail) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.loginRateLimiterService = loginRateLimiterService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.refreshTokenService = refreshTokenService;
        this.autoVerifyEmail = autoVerifyEmail;
    }

    /**
     * Registriert einen neuen Kunden.
     *
     * @param request Registrierungsdaten
     * @return JWT-Token und Kundendaten
     * @throws DuplicateEmailException wenn E-Mail bereits existiert
     * @throws DuplicateDriverLicenseException wenn Führerscheinnummer bereits existiert
     */
    public AuthenticationResponseDTO registerCustomer(RegisterCustomerRequestDTO request) {
        // Validierung: E-Mail und Führerscheinnummer müssen eindeutig sein
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        if (customerRepository.existsByDriverLicenseNumber(request.getDriverLicenseNumber())) {
            throw new DuplicateDriverLicenseException(request.getDriverLicenseNumber());
        }

        // Passwort hashen
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Customer-Entity erstellen
        Address address = new Address(
                request.getStreet(),
                request.getPostalCode(),
                request.getCity()
        );
        DriverLicenseNumber driverLicense = new DriverLicenseNumber(request.getDriverLicenseNumber());

        Customer customer = new Customer(
                request.getFirstName(),
                request.getLastName(),
                address,
                driverLicense,
                request.getEmail(),
                request.getPhoneNumber(),
                hashedPassword
        );

        // Verifikations-Token generieren
        String verificationToken = customer.generateVerificationToken();

        // Customer speichern (ERST speichern, damit Entity im Persistence Context ist)
        customer = customerRepository.save(customer);
        
        // DEVELOPMENT: Auto-verify E-Mail wenn konfiguriert
        if (autoVerifyEmail) {
            customer.verifyEmail(verificationToken);
            customer = customerRepository.save(customer); // Nochmal speichern nach Verifizierung
        }

        // Verifikations-E-Mail versenden (auch wenn auto-verify aktiv - für Logs)
        emailService.sendVerificationEmail(
                customer.getEmail(),
                customer.getFullName(),
                verificationToken
        );

        // JWT-Token und Refresh-Token generieren
        String jwtToken = jwtUtil.generateToken(customer.getEmail(), customer.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer);

        return new AuthenticationResponseDTO(jwtToken, refreshToken.getToken(), customer.getId(), customer.getEmail());
    }

    /**
     * Authentifiziert einen Kunden und generiert JWT-Token.
     *
     * @param request Login-Daten
     * @return JWT-Token und Kundendaten
     * @throws org.springframework.security.core.AuthenticationException wenn Authentifizierung fehlschlägt
     * @throws com.rentacar.domain.exception.TooManyLoginAttemptsException wenn zu viele fehlgeschlagene Versuche
     */
    public AuthenticationResponseDTO authenticateCustomer(LoginRequestDTO request) {
        // Rate Limiting prüfen (wirft TooManyLoginAttemptsException bei Überschreitung)
        loginRateLimiterService.checkLoginAttempt(request.getEmail());

        // Authentifizierung mit Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Customer laden
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException(request.getEmail()));

        // JWT-Token und Refresh-Token generieren
        String jwtToken = jwtUtil.generateToken(customer.getEmail(), customer.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customer);

        // Bei erfolgreichem Login: Counter zurücksetzen
        loginRateLimiterService.resetLoginAttempts(request.getEmail());

        return new AuthenticationResponseDTO(jwtToken, refreshToken.getToken(), customer.getId(), customer.getEmail());
    }

    /**
     * Lädt das Kundenprofil anhand der E-Mail-Adresse.
     *
     * @param email E-Mail-Adresse des Kunden
     * @return Kundenprofil
     * @throws CustomerNotFoundException wenn Kunde nicht gefunden
     */
    @Transactional(readOnly = true)
    public CustomerProfileResponseDTO getCustomerProfile(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(email));

        return mapToProfileResponse(customer);
    }

    /**
     * Aktualisiert das Kundenprofil.
     *
     * @param email E-Mail-Adresse des aktuellen Kunden
     * @param request Aktualisierungsdaten
     * @return Aktualisiertes Kundenprofil
     * @throws CustomerNotFoundException wenn Kunde nicht gefunden
     * @throws DuplicateEmailException wenn neue E-Mail bereits existiert
     */
    public CustomerProfileResponseDTO updateCustomerProfile(String email,
                                                           UpdateCustomerProfileRequestDTO request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(email));

        // Prüfe, ob neue E-Mail bereits von anderem Kunden verwendet wird
        if (!customer.getEmail().equals(request.getEmail()) &&
            customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // Adresse aktualisieren
        Address newAddress = new Address(
                request.getStreet(),
                request.getPostalCode(),
                request.getCity()
        );
        customer.updateAddress(newAddress);

        // Kontaktdaten aktualisieren
        customer.updateContactDetails(request.getEmail(), request.getPhoneNumber());

        // Speichern
        customer = customerRepository.save(customer);

        // Wenn E-Mail geändert wurde, neuen Verifikations-Token senden
        if (!email.equals(request.getEmail())) {
            String verificationToken = customer.generateVerificationToken();
            customer = customerRepository.save(customer);
            emailService.sendVerificationEmail(
                    customer.getEmail(),
                    customer.getFullName(),
                    verificationToken
            );
        }

        return mapToProfileResponse(customer);
    }

    /**
     * Verifiziert die E-Mail-Adresse eines Kunden.
     *
     * @param token Verifikations-Token
     * @throws InvalidVerificationTokenException wenn Token ungültig
     * @throws ExpiredVerificationTokenException wenn Token abgelaufen
     */
    public void verifyEmail(String token) {
        Customer customer = customerRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException(token));

        // Token-Ablauf prüfen
        if (customer.getTokenExpiryDate() == null ||
            LocalDateTime.now().isAfter(customer.getTokenExpiryDate())) {
            throw new ExpiredVerificationTokenException(token);
        }

        // E-Mail verifizieren
        customer.verifyEmail(token);
        customerRepository.save(customer);

        // Willkommens-E-Mail senden
        emailService.sendWelcomeEmail(customer.getEmail(), customer.getFullName());
    }

    /**
     * Loggt einen Kunden aus, indem der JWT-Token auf die Blacklist gesetzt wird
     * und alle Refresh-Tokens widerrufen werden.
     * Die Tokens sind ab diesem Zeitpunkt ungültig, auch wenn sie noch nicht abgelaufen sind.
     *
     * @param token JWT-Token der invalidiert werden soll
     */
    public void logoutCustomer(String token) {
        // Berechne verbleibende Token-Gültigkeit
        var expiration = jwtUtil.extractExpiration(token);
        var now = new java.util.Date();
        var remainingValidity = java.time.Duration.ofMillis(expiration.getTime() - now.getTime());

        // Token zur Blacklist hinzufügen
        tokenBlacklistService.blacklistToken(token, remainingValidity);

        // Alle Refresh-Tokens des Kunden widerrufen
        Long customerId = jwtUtil.extractCustomerId(token);
        refreshTokenService.revokeAllTokensByCustomerId(customerId);
    }

    /**
     * Erneuert den Access-Token mittels eines gültigen Refresh-Tokens.
     *
     * @param refreshTokenString Refresh-Token
     * @return Neuer Access-Token und neuer Refresh-Token
     * @throws RefreshTokenService.RefreshTokenException wenn Refresh-Token ungültig ist
     */
    public AuthenticationResponseDTO refreshAccessToken(String refreshTokenString) {
        // Refresh-Token validieren
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenString);

        // Customer laden
        Customer customer = refreshToken.getCustomer();

        // Alten Refresh-Token widerrufen
        refreshTokenService.revokeRefreshToken(refreshTokenString);

        // Neuen JWT-Token und Refresh-Token generieren
        String newJwtToken = jwtUtil.generateToken(customer.getEmail(), customer.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(customer);

        return new AuthenticationResponseDTO(newJwtToken, newRefreshToken.getToken(), customer.getId(), customer.getEmail());
    }

    private CustomerProfileResponseDTO mapToProfileResponse(Customer customer) {
        return new CustomerProfileResponseDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getAddress().getStreet(),
                customer.getAddress().getPostalCode(),
                customer.getAddress().getCity(),
                customer.getDriverLicenseNumber().getNumber(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.isEmailVerified(),
                customer.getCreatedAt(),
                customer.getRole() != null ? customer.getRole().name() : "CUSTOMER"
        );
    }
}

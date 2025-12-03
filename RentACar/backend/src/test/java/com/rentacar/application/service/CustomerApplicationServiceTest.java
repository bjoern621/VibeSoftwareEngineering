package com.rentacar.application.service;

import com.rentacar.domain.exception.*;
import com.rentacar.domain.model.Address;
import com.rentacar.domain.model.Customer;
import com.rentacar.domain.model.DriverLicenseNumber;
import com.rentacar.domain.repository.CustomerRepository;
import com.rentacar.domain.service.EmailService;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerApplicationServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;

    private CustomerApplicationService customerApplicationService;

    @BeforeEach
    void setUp() {
        customerApplicationService = new CustomerApplicationService(
                customerRepository,
                passwordEncoder,
                authenticationManager,
                jwtUtil,
                emailService,
                false // autoVerifyEmail
        );
    }

    @Test
    void registerCustomer_Success() {
        RegisterCustomerRequestDTO request = createRegisterRequest(
                "John", "Doe", "Street 1", "12345", "City",
                "DL123456789", "john@example.com", "123456789", "password"
        );

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByDriverLicenseNumber(request.getDriverLicenseNumber())).thenReturn(false);
        String encodedPassword = "$2a$10$DUMMYHASHFORTESTINGPURPOSESONLY1234567890123456789012";
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        
        Customer savedCustomer = new Customer(
                request.getFirstName(), request.getLastName(),
                new Address(request.getStreet(), request.getPostalCode(), request.getCity()),
                new DriverLicenseNumber(request.getDriverLicenseNumber()),
                request.getEmail(), request.getPhoneNumber(), encodedPassword
        );
        // Simulate ID generation
        try {
            java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedCustomer, 1L);
        } catch (Exception e) {
            fail("Could not set ID");
        }

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("jwt-token");

        AuthenticationResponseDTO response = customerApplicationService.registerCustomer(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getCustomerId());
        assertEquals("john@example.com", response.getEmail());

        verify(emailService).sendVerificationEmail(eq("john@example.com"), eq("John Doe"), anyString());
    }

    @Test
    void registerCustomer_DuplicateEmail() {
        RegisterCustomerRequestDTO request = createRegisterRequest(
                "John", "Doe", "Street 1", "12345", "City",
                "DL123456789", "john@example.com", "123456789", "password"
        );

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> customerApplicationService.registerCustomer(request));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerCustomer_DuplicateDriverLicense() {
        RegisterCustomerRequestDTO request = createRegisterRequest(
                "John", "Doe", "Street 1", "12345", "City",
                "DL123456789", "john@example.com", "123456789", "password"
        );

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.existsByDriverLicenseNumber(request.getDriverLicenseNumber())).thenReturn(true);

        assertThrows(DuplicateDriverLicenseException.class, () -> customerApplicationService.registerCustomer(request));
        verify(customerRepository, never()).save(any());
    }

    // Helper-Methode zum Erstellen von RegisterCustomerRequestDTO
    private RegisterCustomerRequestDTO createRegisterRequest(
            String firstName, String lastName, String street, String postalCode, String city,
            String driverLicenseNumber, String email, String phoneNumber, String password) {
        RegisterCustomerRequestDTO dto = new RegisterCustomerRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setStreet(street);
        dto.setPostalCode(postalCode);
        dto.setCity(city);
        dto.setDriverLicenseNumber(driverLicenseNumber);
        dto.setEmail(email);
        dto.setPhoneNumber(phoneNumber);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void authenticateCustomer_Success() {
        LoginRequestDTO request = new LoginRequestDTO("john@example.com", "password");
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(1L);
        when(customer.getEmail()).thenReturn("john@example.com");

        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(customer));
        when(jwtUtil.generateToken("john@example.com", 1L)).thenReturn("jwt-token");

        AuthenticationResponseDTO response = customerApplicationService.authenticateCustomer(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticateCustomer_NotFound() {
        LoginRequestDTO request = new LoginRequestDTO("john@example.com", "password");
        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerApplicationService.authenticateCustomer(request));
    }

    @Test
    void getCustomerProfile_Success() {
        String email = "john@example.com";
        String encodedPassword = "$2a$10$DUMMYHASHFORTESTINGPURPOSESONLY1234567890123456789012";
        Customer customer = new Customer(
                "John", "Doe",
                new Address("Street", "12345", "City"),
                new DriverLicenseNumber("DL123456789"),
                email, "123456", encodedPassword
        );
        // Set ID
        try {
            java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(customer, 1L);
        } catch (Exception e) {
            fail("Could not set ID");
        }

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        CustomerProfileResponseDTO response = customerApplicationService.getCustomerProfile(email);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void updateCustomerProfile_Success() {
        String email = "john@example.com";
        UpdateCustomerProfileRequestDTO request = new UpdateCustomerProfileRequestDTO(
                "John", "Doe", "New Street", "54321", "New City", "john@example.com", "987654321"
        );

        String encodedPassword = "$2a$10$DUMMYHASHFORTESTINGPURPOSESONLY1234567890123456789012";
        Customer customer = new Customer(
                "John", "Doe",
                new Address("Street", "12345", "City"),
                new DriverLicenseNumber("DL123456789"),
                email, "123456", encodedPassword
        );
        
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerProfileResponseDTO response = customerApplicationService.updateCustomerProfile(email, request);

        assertEquals("New Street", response.getStreet());
        assertEquals("987654321", response.getPhoneNumber());
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void updateCustomerProfile_EmailChanged() {
        String email = "john@example.com";
        String newEmail = "john.new@example.com";
        UpdateCustomerProfileRequestDTO request = new UpdateCustomerProfileRequestDTO(
                "John", "Doe", "Street", "12345", "City", newEmail, "123456"
        );

        String encodedPassword = "$2a$10$DUMMYHASHFORTESTINGPURPOSESONLY1234567890123456789012";
        Customer customer = new Customer(
                "John", "Doe",
                new Address("Street", "12345", "City"),
                new DriverLicenseNumber("DL123456789"),
                email, "123456", encodedPassword
        );

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail(newEmail)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerApplicationService.updateCustomerProfile(email, request);

        verify(emailService).sendVerificationEmail(eq(newEmail), anyString(), anyString());
    }

    @Test
    void verifyEmail_Success() {
        String token = "valid-token";
        Customer customer = mock(Customer.class);
        when(customer.getTokenExpiryDate()).thenReturn(LocalDateTime.now().plusHours(1));
        when(customer.getEmail()).thenReturn("john@example.com");
        when(customer.getFullName()).thenReturn("John Doe");

        when(customerRepository.findByVerificationToken(token)).thenReturn(Optional.of(customer));

        customerApplicationService.verifyEmail(token);

        verify(customer).verifyEmail(token);
        verify(customerRepository).save(customer);
        verify(emailService).sendWelcomeEmail("john@example.com", "John Doe");
    }

    @Test
    void verifyEmail_InvalidToken() {
        String token = "invalid-token";
        when(customerRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        assertThrows(InvalidVerificationTokenException.class, () -> customerApplicationService.verifyEmail(token));
    }

    @Test
    void verifyEmail_ExpiredToken() {
        String token = "expired-token";
        Customer customer = mock(Customer.class);
        when(customer.getTokenExpiryDate()).thenReturn(LocalDateTime.now().minusHours(1));

        when(customerRepository.findByVerificationToken(token)).thenReturn(Optional.of(customer));

        assertThrows(ExpiredVerificationTokenException.class, () -> customerApplicationService.verifyEmail(token));
    }
}

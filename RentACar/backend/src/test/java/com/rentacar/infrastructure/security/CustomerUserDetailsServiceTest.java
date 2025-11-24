package com.rentacar.infrastructure.security;

import com.rentacar.domain.model.Customer;
import com.rentacar.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerUserDetailsService customerUserDetailsService;

    @Test
    void loadUserByUsername_Success() {
        String email = "test@example.com";
        Customer customer = mock(Customer.class);
        when(customer.getEmail()).thenReturn(email);
        when(customer.getPassword()).thenReturn("encodedPassword");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    void loadUserByUsername_NotFound() {
        String email = "test@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customerUserDetailsService.loadUserByUsername(email));
    }

    @Test
    void loadUserById_Success() {
        Long id = 1L;
        Customer customer = mock(Customer.class);
        when(customer.getEmail()).thenReturn("test@example.com");
        when(customer.getPassword()).thenReturn("encodedPassword");

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        UserDetails userDetails = customerUserDetailsService.loadUserById(id);

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    void loadUserById_NotFound() {
        Long id = 1L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customerUserDetailsService.loadUserById(id));
    }
}

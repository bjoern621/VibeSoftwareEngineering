package com.rentacar.infrastructure.security;

import com.rentacar.domain.model.Customer;
import com.rentacar.domain.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService-Implementierung für Customer-Authentication.
 * Lädt Kundendaten für Spring Security Authentication.
 */
@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kunde mit E-Mail '" + email + "' nicht gefunden"));

        String roleName = customer.getRole() != null ? customer.getRole().name() : "CUSTOMER";
        return User.builder()
            .username(customer.getEmail())
            .password(customer.getPassword())
            .roles(roleName) // Rolle dynamisch aus Customer
            .accountLocked(false) // DEVELOPMENT: Account-Lock deaktiviert für einfaches Testen
            .build();
    }

    /**
     * Lädt einen Customer anhand der ID (für JWT-Token-Validierung).
     *
     * @param customerId Kunden-ID
     * @return UserDetails
     * @throws UsernameNotFoundException wenn Kunde nicht gefunden
     */
    public UserDetails loadUserById(Long customerId) throws UsernameNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kunde mit ID '" + customerId + "' nicht gefunden"));

        String roleName = customer.getRole() != null ? customer.getRole().name() : "CUSTOMER";
        return User.builder()
            .username(customer.getEmail())
            .password(customer.getPassword())
            .roles(roleName)
            .accountLocked(false) // DEVELOPMENT: Account-Lock deaktiviert für einfaches Testen
            .build();
    }
}

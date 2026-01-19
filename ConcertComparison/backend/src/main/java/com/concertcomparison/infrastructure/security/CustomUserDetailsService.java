package com.concertcomparison.infrastructure.security;

import com.concertcomparison.domain.model.User;
import com.concertcomparison.domain.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetailsService für Spring Security.
 * 
 * Lädt User-Daten aus der Datenbank und konvertiert sie zu Spring Security UserDetails.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Lädt User anhand der Email (Username).
     * 
     * @param email Email/Username
     * @return UserDetails für Spring Security
     * @throws UsernameNotFoundException wenn User nicht gefunden
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User mit Email nicht gefunden: " + email));
        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }
    
    /**
     * Konvertiert UserRole zu Spring Security GrantedAuthority.
     * 
     * @param user User Entity
     * @return Collection von GrantedAuthorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}

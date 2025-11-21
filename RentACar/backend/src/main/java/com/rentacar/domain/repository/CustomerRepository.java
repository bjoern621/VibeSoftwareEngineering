package com.rentacar.domain.repository;

import com.rentacar.domain.model.Customer;
import java.util.Optional;

/**
 * Repository Interface für Customer Aggregate.
 * Port für die Infrastructure Layer Implementation.
 */
public interface CustomerRepository {

    /**
     * Speichert einen Kunden.
     *
     * @param customer zu speichernder Kunde
     * @return gespeicherter Kunde mit ID
     */
    Customer save(Customer customer);

    /**
     * Sucht einen Kunden anhand seiner ID.
     *
     * @param id Kunden-ID
     * @return Optional mit Kunde, falls gefunden
     */
    Optional<Customer> findById(Long id);

    /**
     * Sucht einen Kunden anhand seiner E-Mail-Adresse.
     *
     * @param email E-Mail-Adresse
     * @return Optional mit Kunde, falls gefunden
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Sucht einen Kunden anhand seiner Führerscheinnummer.
     *
     * @param licenseNumber Führerscheinnummer
     * @return Optional mit Kunde, falls gefunden
     */
    Optional<Customer> findByDriverLicenseNumber(String licenseNumber);

    /**
     * Prüft, ob eine E-Mail-Adresse bereits registriert ist.
     *
     * @param email E-Mail-Adresse
     * @return true, wenn E-Mail existiert
     */
    boolean existsByEmail(String email);

    /**
     * Prüft, ob eine Führerscheinnummer bereits registriert ist.
     *
     * @param licenseNumber Führerscheinnummer
     * @return true, wenn Führerscheinnummer existiert
     */
    boolean existsByDriverLicenseNumber(String licenseNumber);
}

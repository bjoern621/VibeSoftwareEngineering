package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.Customer;
import com.rentacar.domain.repository.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository Implementation für Customer.
 * Implementiert die Domain Repository Schnittstelle.
 */
@Repository
public interface JpaCustomerRepository extends CustomerRepository, JpaRepository<Customer, Long> {

    @Override
    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    @Override
    @Query("SELECT c FROM Customer c WHERE c.driverLicenseNumber.number = :licenseNumber")
    Optional<Customer> findByDriverLicenseNumber(@Param("licenseNumber") String licenseNumber);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.driverLicenseNumber.number = :licenseNumber")
    boolean existsByDriverLicenseNumber(@Param("licenseNumber") String licenseNumber);
}

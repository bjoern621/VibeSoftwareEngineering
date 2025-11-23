package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.RentalAgreement;
import com.rentacar.domain.repository.RentalAgreementRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRentalAgreementRepository extends RentalAgreementRepository, JpaRepository<RentalAgreement, Long> {
    Optional<RentalAgreement> findByBookingId(Long bookingId);
}

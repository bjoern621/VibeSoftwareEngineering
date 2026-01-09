package com.rentacar.domain.repository;

import com.rentacar.domain.model.RentalAgreement;
import java.util.Optional;

public interface RentalAgreementRepository {
    RentalAgreement save(RentalAgreement rentalAgreement);
    Optional<RentalAgreement> findById(Long id);
    Optional<RentalAgreement> findByBookingId(Long bookingId);
}

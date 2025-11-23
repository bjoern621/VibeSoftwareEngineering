package com.rentacar.domain.repository;

import com.rentacar.domain.model.DamageReport;
import java.util.List;
import java.util.Optional;

public interface DamageReportRepository {
    DamageReport save(DamageReport damageReport);
    Optional<DamageReport> findById(Long id);
    List<DamageReport> findByRentalAgreementId(Long rentalAgreementId);
}

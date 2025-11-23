package com.rentacar.infrastructure.persistence;

import com.rentacar.domain.model.DamageReport;
import com.rentacar.domain.repository.DamageReportRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDamageReportRepository extends DamageReportRepository, JpaRepository<DamageReport, Long> {
}

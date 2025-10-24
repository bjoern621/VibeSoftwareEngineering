package com.mymensa2.backend.staff.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Integer> {
    
    // Findet alle Arbeitszeiten in einem Zeitraum
    @Query("SELECT wh FROM WorkingHours wh WHERE wh.date BETWEEN :startDate AND :endDate")
    List<WorkingHours> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Findet Arbeitszeiten für einen Mitarbeiter in einem Zeitraum
    @Query("SELECT wh FROM WorkingHours wh WHERE wh.staffId = :staffId AND wh.date BETWEEN :startDate AND :endDate")
    List<WorkingHours> findByStaffIdAndDateBetween(Integer staffId, LocalDate startDate, LocalDate endDate);
}

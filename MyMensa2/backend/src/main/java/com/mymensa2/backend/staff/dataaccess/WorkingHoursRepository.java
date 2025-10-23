package com.mymensa2.backend.staff.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Integer> {
    
    @Query("SELECT w FROM WorkingHours w WHERE w.date BETWEEN :startDate AND :endDate")
    List<WorkingHours> findByDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT w FROM WorkingHours w WHERE w.staff.id = :staffId AND w.date BETWEEN :startDate AND :endDate")
    List<WorkingHours> findByStaffIdAndDateBetween(@Param("staffId") Integer staffId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}

package com.mymensa2.backend.staff.dataaccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    
    List<Staff> findByRole(StaffRole role);
    
    List<Staff> findByIsAvailable(Boolean isAvailable);
    
    List<Staff> findByRoleAndIsAvailable(StaffRole role, Boolean isAvailable);
}

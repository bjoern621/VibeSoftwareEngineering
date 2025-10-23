package com.mymensa2.backend.staff.logic;

import com.mymensa2.backend.staff.dataaccess.*;
import com.mymensa2.backend.staff.facade.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StaffService {
    
    private final StaffRepository staffRepository;
    private final WorkingHoursRepository workingHoursRepository;
    
    public StaffService(StaffRepository staffRepository, WorkingHoursRepository workingHoursRepository) {
        this.staffRepository = staffRepository;
        this.workingHoursRepository = workingHoursRepository;
    }
    
    // Alle Mitarbeiter abrufen mit optionalen Filtern
    public List<StaffDTO> getAllStaff(String role, Boolean available) {
        List<Staff> staffList;
        
        if (role != null && available != null) {
            StaffRole staffRole = StaffRole.valueOf(role.toUpperCase());
            staffList = staffRepository.findByRoleAndIsAvailable(staffRole, available);
        } else if (role != null) {
            StaffRole staffRole = StaffRole.valueOf(role.toUpperCase());
            staffList = staffRepository.findByRole(staffRole);
        } else if (available != null) {
            staffList = staffRepository.findByIsAvailable(available);
        } else {
            staffList = staffRepository.findAll();
        }
        
        return staffList.stream()
            .map(this::toDTO)
            .toList();
    }
    
    // Neuen Mitarbeiter erstellen (mit simulierter STAFFMAN-Synchronisation)
    @Transactional
    public StaffDTO createStaff(StaffRequestDTO request) {
        StaffRole role = StaffRole.valueOf(request.role().toUpperCase());
        
        // Simulierte STAFFMAN-ID generieren
        String staffmanId = "STAFFMAN-EMP-" + System.currentTimeMillis();
        
        Staff staff = new Staff(
            request.firstName(),
            request.lastName(),
            role,
            staffmanId
        );
        
        Staff saved = staffRepository.save(staff);
        return toDTO(saved);
    }
    
    // Arbeitszeiten erfassen
    @Transactional
    public WorkingHoursDTO recordWorkingHours(Integer staffId, WorkingHoursRequestDTO request) {
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Mitarbeiter nicht gefunden"));
        
        LocalDate date = LocalDate.parse(request.date(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime startTime = LocalTime.parse(request.startTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime endTime = LocalTime.parse(request.endTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        WorkingHours workingHours = new WorkingHours(staff, date, startTime, endTime);
        WorkingHours saved = workingHoursRepository.save(workingHours);
        
        return toWorkingHoursDTO(saved);
    }
    
    // Arbeitszeiten für Zeitraum abrufen
    public List<WorkingHoursDTO> getWorkingHours(String startDate, String endDate, Integer staffId) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        List<WorkingHours> workingHoursList;
        
        if (staffId != null) {
            workingHoursList = workingHoursRepository.findByStaffIdAndDateBetween(staffId, start, end);
        } else {
            workingHoursList = workingHoursRepository.findByDateBetween(start, end);
        }
        
        return workingHoursList.stream()
            .map(this::toWorkingHoursDTO)
            .toList();
    }
    
    // Einsatzplanung-Empfehlung basierend auf erwarteter Besucherzahl
    public ScheduleRecommendationDTO getScheduleRecommendation(String date) {
        // Vereinfachte Logik: Erwartete Besucher = 350 (simuliert)
        Integer expectedVisitors = 350;
        Integer plannedMeals = 5; // Vereinfacht
        
        // Empfohlene Anzahl: 1 Koch pro 100 Besucher, 1 Service pro 80 Besucher
        Integer recommendedCooks = (int) Math.ceil(expectedVisitors / 100.0);
        Integer recommendedService = (int) Math.ceil(expectedVisitors / 80.0);
        Integer recommendedTotal = recommendedCooks + recommendedService;
        
        RecommendedStaffDTO recommendedStaff = new RecommendedStaffDTO(
            recommendedCooks,
            recommendedService,
            recommendedTotal
        );
        
        // Verfügbare Mitarbeiter abrufen
        List<StaffDTO> availableCooks = staffRepository.findByRoleAndIsAvailable(StaffRole.COOK, true)
            .stream()
            .map(this::toDTO)
            .toList();
        
        List<StaffDTO> availableService = staffRepository.findByRoleAndIsAvailable(StaffRole.SERVICE, true)
            .stream()
            .map(this::toDTO)
            .toList();
        
        AvailableStaffDTO availableStaff = new AvailableStaffDTO(availableCooks, availableService);
        
        return new ScheduleRecommendationDTO(
            date,
            expectedVisitors,
            plannedMeals,
            recommendedStaff,
            availableStaff
        );
    }
    
    // Hilfsmethoden: Entity zu DTO
    private StaffDTO toDTO(Staff staff) {
        return new StaffDTO(
            staff.getId(),
            staff.getFirstName(),
            staff.getLastName(),
            staff.getRole().name(),
            staff.getStaffmanId(),
            staff.getIsAvailable()
        );
    }
    
    private WorkingHoursDTO toWorkingHoursDTO(WorkingHours workingHours) {
        return new WorkingHoursDTO(
            workingHours.getId(),
            workingHours.getStaff().getId(),
            toDTO(workingHours.getStaff()),
            workingHours.getDate().toString(),
            workingHours.getStartTime().toString(),
            workingHours.getEndTime().toString(),
            workingHours.getHoursWorked(),
            workingHours.getSyncedToStaffman()
        );
    }
}

package com.mymensa2.backend.staff.logic;

import com.mymensa2.backend.common.InvalidRequestException;
import com.mymensa2.backend.common.ResourceNotFoundException;
import com.mymensa2.backend.mealplans.dataaccess.MealPlan;
import com.mymensa2.backend.mealplans.dataaccess.MealPlanRepository;
import com.mymensa2.backend.staff.dataaccess.Staff;
import com.mymensa2.backend.staff.dataaccess.StaffRepository;
import com.mymensa2.backend.staff.dataaccess.WorkingHours;
import com.mymensa2.backend.staff.dataaccess.WorkingHoursRepository;
import com.mymensa2.backend.staff.facade.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StaffService {
    
    private final StaffRepository staffRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final MealPlanRepository mealPlanRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public StaffService(StaffRepository staffRepository, WorkingHoursRepository workingHoursRepository, 
                        MealPlanRepository mealPlanRepository) {
        this.staffRepository = staffRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.mealPlanRepository = mealPlanRepository;
    }
    
    // Alle Mitarbeiter abrufen
    @Transactional(readOnly = true)
    public List<StaffResponseDTO> getAllStaff(String role, Boolean available) {
        List<Staff> staff;
        
        if (role != null && available != null && available) {
            staff = staffRepository.findByRoleAndAvailable(role);
        } else if (role != null) {
            staff = staffRepository.findByRole(role);
        } else if (available != null && available) {
            staff = staffRepository.findAvailable();
        } else {
            staff = staffRepository.findAll();
        }
        
        return staff.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Mitarbeiter erstellen
    @Transactional
    public StaffResponseDTO createStaff(StaffRequestDTO request) {
        validateStaffRequest(request);
        
        // Simuliere STAFFMAN-Synchronisation mit generierter ID
        String staffmanId = "STAFFMAN-EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Staff staff = new Staff(
                request.firstName(),
                request.lastName(),
                request.role(),
                staffmanId
        );
        
        Staff savedStaff = staffRepository.save(staff);
        return convertToDTO(savedStaff);
    }
    
    // Arbeitszeiten erfassen
    @Transactional
    public WorkingHoursResponseDTO recordWorkingHours(Integer staffId, WorkingHoursRequestDTO request) {
        validateWorkingHoursRequest(request);
        
        // Prüfe, ob Mitarbeiter existiert
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Mitarbeiter mit ID " + staffId + " nicht gefunden"));
        
        LocalDate date = parseDate(request.date());
        LocalTime startTime = parseTime(request.startTime());
        LocalTime endTime = parseTime(request.endTime());
        
        if (endTime.isBefore(startTime)) {
            throw new InvalidRequestException("Endzeit darf nicht vor Startzeit liegen");
        }
        
        WorkingHours workingHours = new WorkingHours(staffId, date, startTime, endTime);
        workingHours.setSyncedToStaffman(true); // Simuliere STAFFMAN-Synchronisation
        
        WorkingHours savedWorkingHours = workingHoursRepository.save(workingHours);
        
        return new WorkingHoursResponseDTO(
                savedWorkingHours.getId(),
                savedWorkingHours.getStaffId(),
                savedWorkingHours.getDate().format(DATE_FORMATTER),
                savedWorkingHours.getStartTime().format(TIME_FORMATTER),
                savedWorkingHours.getEndTime().format(TIME_FORMATTER),
                savedWorkingHours.getHoursWorked(),
                savedWorkingHours.getSyncedToStaffman()
        );
    }
    
    // Arbeitszeiten für Zeitraum abrufen
    @Transactional(readOnly = true)
    public List<WorkingHoursWithStaffDTO> getWorkingHours(String startDateStr, String endDateStr, Integer staffId) {
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);
        
        List<WorkingHours> workingHours;
        
        if (staffId != null) {
            workingHours = workingHoursRepository.findByStaffIdAndDateBetween(staffId, startDate, endDate);
        } else {
            workingHours = workingHoursRepository.findByDateBetween(startDate, endDate);
        }
        
        return workingHours.stream()
                .map(wh -> new WorkingHoursWithStaffDTO(
                        wh.getId(),
                        convertToDTO(wh.getStaff()),
                        wh.getDate().format(DATE_FORMATTER),
                        wh.getStartTime().format(TIME_FORMATTER),
                        wh.getEndTime().format(TIME_FORMATTER),
                        wh.getHoursWorked(),
                        wh.getSyncedToStaffman()
                ))
                .collect(Collectors.toList());
    }
    
    // Einsatzplanung basierend auf erwarteter Besucherzahl
    @Transactional(readOnly = true)
    public ScheduleRecommendationDTO getScheduleRecommendation(String dateStr) {
        LocalDate date = parseDate(dateStr);
        
        // Zähle geplante Gerichte für diesen Tag
        List<MealPlan> mealPlans = mealPlanRepository.findByDateBetween(date, date);
        int plannedMeals = mealPlans.size();
        
        // Schätze Besucherzahl basierend auf Gesamtbestand
        int expectedVisitors = mealPlans.stream()
                .mapToInt(MealPlan::getStock)
                .sum();
        
        // Berechne empfohlene Mitarbeiteranzahl (vereinfachte Logik)
        // Pro 50 Besucher 1 Koch, pro 100 Besucher 1 Service-Kraft
        int recommendedCooks = Math.max(2, (expectedVisitors / 50) + 1);
        int recommendedService = Math.max(2, (expectedVisitors / 100) + 2);
        int totalRecommended = recommendedCooks + recommendedService;
        
        // Hole verfügbare Mitarbeiter
        List<StaffResponseDTO> availableCooks = staffRepository.findByRoleAndAvailable("COOK").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        List<StaffResponseDTO> availableService = staffRepository.findByRoleAndAvailable("SERVICE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new ScheduleRecommendationDTO(
                dateStr,
                expectedVisitors,
                plannedMeals,
                new RecommendedStaffDTO(recommendedCooks, recommendedService, totalRecommended),
                new StaffByRoleDTO(availableCooks, availableService)
        );
    }
    
    // Hilfsmethode: Datum parsen
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Ungültiges Datumsformat. Erwartet: yyyy-MM-dd");
        }
    }
    
    // Hilfsmethode: Zeit parsen
    private LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Ungültiges Zeitformat. Erwartet: HH:mm:ss");
        }
    }
    
    // Validierung der Staff-Request-Daten
    private void validateStaffRequest(StaffRequestDTO request) {
        if (request.firstName() == null || request.firstName().trim().isEmpty()) {
            throw new InvalidRequestException("Vorname ist erforderlich");
        }
        if (request.lastName() == null || request.lastName().trim().isEmpty()) {
            throw new InvalidRequestException("Nachname ist erforderlich");
        }
        if (request.role() == null || request.role().trim().isEmpty()) {
            throw new InvalidRequestException("Rolle ist erforderlich");
        }
        
        List<String> validRoles = List.of("COOK", "SERVICE", "MANAGER");
        if (!validRoles.contains(request.role())) {
            throw new InvalidRequestException("Ungültige Rolle. Erlaubt: " + String.join(", ", validRoles));
        }
    }
    
    // Validierung der WorkingHours-Request-Daten
    private void validateWorkingHoursRequest(WorkingHoursRequestDTO request) {
        if (request.date() == null || request.date().trim().isEmpty()) {
            throw new InvalidRequestException("Datum ist erforderlich");
        }
        if (request.startTime() == null || request.startTime().trim().isEmpty()) {
            throw new InvalidRequestException("Startzeit ist erforderlich");
        }
        if (request.endTime() == null || request.endTime().trim().isEmpty()) {
            throw new InvalidRequestException("Endzeit ist erforderlich");
        }
    }
    
    // Konvertierung von Entity zu DTO
    private StaffResponseDTO convertToDTO(Staff staff) {
        return new StaffResponseDTO(
                staff.getId(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getRole(),
                staff.getStaffmanId(),
                staff.getIsAvailable()
        );
    }
}

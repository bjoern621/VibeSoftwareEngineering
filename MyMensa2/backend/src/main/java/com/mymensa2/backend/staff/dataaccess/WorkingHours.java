package com.mymensa2.backend.staff.dataaccess;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "working_hours")
public class WorkingHours {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private Integer staffId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staffId", insertable = false, updatable = false)
    private Staff staff;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private Float hoursWorked;
    
    @Column(nullable = false)
    private Boolean syncedToStaffman = false;
    
    // Constructors
    public WorkingHours() {
    }
    
    public WorkingHours(Integer staffId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.staffId = staffId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hoursWorked = calculateHoursWorked(startTime, endTime);
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getStaffId() {
        return staffId;
    }
    
    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }
    
    public Staff getStaff() {
        return staff;
    }
    
    public void setStaff(Staff staff) {
        this.staff = staff;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        this.hoursWorked = calculateHoursWorked(startTime, this.endTime);
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
        this.hoursWorked = calculateHoursWorked(this.startTime, endTime);
    }
    
    public Float getHoursWorked() {
        return hoursWorked;
    }
    
    public void setHoursWorked(Float hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
    
    public Boolean getSyncedToStaffman() {
        return syncedToStaffman;
    }
    
    public void setSyncedToStaffman(Boolean syncedToStaffman) {
        this.syncedToStaffman = syncedToStaffman;
    }
    
    // Berechne Arbeitsstunden
    private Float calculateHoursWorked(LocalTime start, LocalTime end) {
        if (start == null || end == null) return 0.0f;
        long minutes = java.time.Duration.between(start, end).toMinutes();
        return minutes / 60.0f;
    }
}

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
    
    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
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
    private Boolean syncedToStaffman = true;
    
    public WorkingHours() {
    }
    
    public WorkingHours(Staff staff, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.staff = staff;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hoursWorked = calculateHoursWorked(startTime, endTime);
        this.syncedToStaffman = true;
    }
    
    // Berechnung der Arbeitsstunden
    private Float calculateHoursWorked(LocalTime start, LocalTime end) {
        long seconds = java.time.Duration.between(start, end).getSeconds();
        return seconds / 3600.0f;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
        if (this.endTime != null) {
            this.hoursWorked = calculateHoursWorked(startTime, this.endTime);
        }
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
        if (this.startTime != null) {
            this.hoursWorked = calculateHoursWorked(this.startTime, endTime);
        }
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
}

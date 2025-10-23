package com.mymensa2.backend.staff.dataaccess;

import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StaffRole role;
    
    @Column(nullable = false, unique = true)
    private String staffmanId;
    
    @Column(nullable = false)
    private Boolean isAvailable = true;
    
    public Staff() {
    }
    
    public Staff(String firstName, String lastName, StaffRole role, String staffmanId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.staffmanId = staffmanId;
        this.isAvailable = true;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public StaffRole getRole() {
        return role;
    }
    
    public void setRole(StaffRole role) {
        this.role = role;
    }
    
    public String getStaffmanId() {
        return staffmanId;
    }
    
    public void setStaffmanId(String staffmanId) {
        this.staffmanId = staffmanId;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}

package com.rentacar.presentation.dto;

import com.rentacar.domain.model.Branch;

/**
 * DTO für Branch (Filiale) Responses.
 */
public class BranchResponseDTO {
    
    private Long id;
    private String name;
    private String address;
    private String openingHours;
    
    public BranchResponseDTO() {
    }
    
    public BranchResponseDTO(Long id, String name, String address, String openingHours) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.openingHours = openingHours;
    }
    
    /**
     * Factory-Methode zum Erstellen eines DTOs aus einer Branch-Entity.
     * 
     * @param branch die Branch-Entity
     * @return das erstellte DTO
     */
    public static BranchResponseDTO fromDomain(Branch branch) {
        return new BranchResponseDTO(
            branch.getId(),
            branch.getName(),
            branch.getAddress(),
            branch.getOpeningHours()
        );
    }
    
    // Getter und Setter
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
}

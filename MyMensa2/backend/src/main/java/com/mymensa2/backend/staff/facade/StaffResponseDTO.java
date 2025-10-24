package com.mymensa2.backend.staff.facade;

public record StaffResponseDTO(
    Integer id,
    String firstName,
    String lastName,
    String role,
    String staffmanId,
    Boolean isAvailable
) {}

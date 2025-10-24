package com.mymensa2.backend.staff.facade;

public record WorkingHoursWithStaffDTO(
    Integer id,
    StaffResponseDTO staff,
    String date,
    String startTime,
    String endTime,
    Float hoursWorked,
    Boolean syncedToStaffman
) {}

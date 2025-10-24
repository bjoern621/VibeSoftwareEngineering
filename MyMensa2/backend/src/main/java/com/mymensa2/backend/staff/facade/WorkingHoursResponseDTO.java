package com.mymensa2.backend.staff.facade;

public record WorkingHoursResponseDTO(
    Integer id,
    Integer staffId,
    String date,
    String startTime,
    String endTime,
    Float hoursWorked,
    Boolean syncedToStaffman
) {}

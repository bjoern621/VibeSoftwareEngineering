package com.mymensa2.backend.staff.facade;

public record WorkingHoursDTO(
    Integer id,
    Integer staffId,
    StaffDTO staff,
    String date,
    String startTime,
    String endTime,
    Float hoursWorked,
    Boolean syncedToStaffman
) {}

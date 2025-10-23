package com.mymensa2.backend.staff.facade;

public record WorkingHoursRequestDTO(
    String date,
    String startTime,
    String endTime
) {}

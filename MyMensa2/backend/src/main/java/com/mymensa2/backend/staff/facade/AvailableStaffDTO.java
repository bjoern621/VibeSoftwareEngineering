package com.mymensa2.backend.staff.facade;

import java.util.List;

public record AvailableStaffDTO(
    List<StaffDTO> cooks,
    List<StaffDTO> service
) {}

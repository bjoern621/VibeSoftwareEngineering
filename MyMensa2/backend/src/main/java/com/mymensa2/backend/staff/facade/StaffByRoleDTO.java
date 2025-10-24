package com.mymensa2.backend.staff.facade;

import java.util.List;

public record StaffByRoleDTO(
    List<StaffResponseDTO> cooks,
    List<StaffResponseDTO> service
) {}

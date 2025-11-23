package com.rentacar.presentation.controller;

import com.rentacar.application.service.DamageReportApplicationService;
import com.rentacar.presentation.dto.CreateDamageReportRequestDTO;
import com.rentacar.presentation.dto.DamageReportResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DamageReportController {

    private final DamageReportApplicationService damageReportApplicationService;

    public DamageReportController(DamageReportApplicationService damageReportApplicationService) {
        this.damageReportApplicationService = damageReportApplicationService;
    }

    @PostMapping("/vermietung/{buchungId}/schadensbericht")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<DamageReportResponseDTO> createDamageReport(
            @PathVariable Long buchungId,
            @Valid @RequestBody CreateDamageReportRequestDTO request) {
        DamageReportResponseDTO response = damageReportApplicationService.createDamageReport(buchungId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/schadensberichte/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<DamageReportResponseDTO> getDamageReport(@PathVariable Long id) {
        DamageReportResponseDTO response = damageReportApplicationService.getDamageReport(id);
        return ResponseEntity.ok(response);
    }
}

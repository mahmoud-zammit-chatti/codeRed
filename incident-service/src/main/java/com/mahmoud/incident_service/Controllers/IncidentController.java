package com.mahmoud.incident_service.Controllers;

import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Services.IncidentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/protected/get-all-pending")
    @SecurityRequirement(name = "Bearer Authentication")

    public ResponseEntity<ArrayList<IncidentResponseDTO>> getPendingIncident(){
        return ResponseEntity.ok(incidentService.getAllPendingIncidents());
    }

    @PostMapping("/public/create")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<IncidentResponseDTO> createIncident(@Valid @RequestBody CreateIncidentRequestDTO request, @AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(incidentService.createIncident(request, jwt));
    }
}
package com.mahmoud.incident_service.Controllers;

import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Services.IncidentService;
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
    public ResponseEntity<ArrayList<IncidentResponseDTO>> getPendingIncident(@AuthenticationPrincipal Jwt jwt){
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return ResponseEntity.ok(incidentService.getAllPendingIncidents());
    }

    @PostMapping("/public/create")
    public ResponseEntity<IncidentResponseDTO> createIncident(@Valid @RequestBody CreateIncidentRequestDTO request){
        return ResponseEntity.ok(incidentService.createIncident(request));
    }
}
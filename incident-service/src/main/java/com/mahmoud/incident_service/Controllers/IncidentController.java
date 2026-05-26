package com.mahmoud.incident_service.Controllers;

import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Services.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/get-all-pending")
    public ResponseEntity<ArrayList<IncidentResponseDTO>> getPendingIncident(){
        return ResponseEntity.ok(incidentService.getAllPendingIncidents());
    }

    @PostMapping("/create")
    public ResponseEntity<IncidentResponseDTO> createIncident(@Valid @RequestBody CreateIncidentRequestDTO request){
        return ResponseEntity.ok(incidentService.createIncident(request));
    }

}

package com.mahmoud.incident_service.Services;

import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentMapper;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;
import com.mahmoud.incident_service.Models.Incident;
import com.mahmoud.incident_service.Models.Severity;
import com.mahmoud.incident_service.Models.Status;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepo incidentRepo;
    private final IncidentMapper incidentMapper;
    public IncidentService(IncidentRepo incidentRepo, IncidentMapper incidentMapper) {
        this.incidentRepo = incidentRepo;
        this.incidentMapper = incidentMapper;
    }

    public IncidentResponseDTO createIncident(CreateIncidentRequestDTO request){
        Incident incident = incidentMapper.toEntity(request);
        incident.setCreatedAt(Instant.now());
        incident.setUpdatedAt(Instant.now());
        Incident savedIncident = incidentRepo.save(incident);
        return incidentMapper.toResponse(savedIncident);
    }

    public IncidentResponseDTO getIncidentById(UUID incidentId){
        Incident incident = incidentRepo.findById(incidentId).orElseThrow(()-> new IncidentNotFoundException("Incident with ID " + incidentId + " not found"));
        return incidentMapper.toResponse(incident);
    }

    public void  setIncidentStatus(UUID incidentId, Status status){
        Incident incident = incidentRepo.findById(incidentId).orElseThrow(()-> new IncidentNotFoundException("Incident with ID " + incidentId + " not found"));
        incident.setStatus(status);
        incidentRepo.save(incident);
    }
    public void setIncidentSeverity(UUID incidentId, Severity severity){
        Incident incident = incidentRepo.findById(incidentId).orElseThrow(()-> new IncidentNotFoundException("Incident with ID " + incidentId + " not found"));
        incident.setSeverity(severity);
        incidentRepo.save(incident);
    }

    public ArrayList<IncidentResponseDTO> getAllPendingIncidents(){

        ArrayList<Incident> incidents = incidentRepo.findAllPendingIncidents();
        ArrayList<IncidentResponseDTO> incidentResponseDTOS = new ArrayList<>();
        for (Incident incident : incidents) {
            incidentResponseDTOS.add(incidentMapper.toResponse(incident));
        }
        return incidentResponseDTOS;
    }




}

package com.mahmoud.incident_service.Services;

import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentMapper;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;
import com.mahmoud.incident_service.Models.Incident;
import com.mahmoud.incident_service.Models.Severity;
import com.mahmoud.incident_service.Models.Status;
import com.mahmoud.incident_service.RabbitMQ.IncidentCreatedMessage;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class IncidentService {

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;
    @Value("${INCIDENT_CREATED_ROUTING_KEY}")
    String INCIDENT_CREATED_ROUTING_KEY;
    @Value("${AI_EVALUATION_QUEUE}")
    String AI_EVALUATION_QUEUE;

    private final IncidentRepo incidentRepo;
    private final IncidentMapper incidentMapper;
    private final AmqpTemplate amqpTemplate;
    public IncidentService(IncidentRepo incidentRepo, IncidentMapper incidentMapper, AmqpTemplate amqpTemplate) {
        this.incidentRepo = incidentRepo;
        this.incidentMapper = incidentMapper;
        this.amqpTemplate = amqpTemplate;
    }

    public IncidentResponseDTO createIncident(CreateIncidentRequestDTO request){
        Incident incident = incidentMapper.toEntity(request);
        incident.setCreatedAt(Instant.now());
        Incident savedIncident = incidentRepo.save(incident);
        //publish incident to RabbitMQ

        //build the payload for sending
        IncidentCreatedMessage incidentCreatedMessage = incidentMapper.toIncidentCreatedMessage(savedIncident);

        //publish the payload with the routing key
        amqpTemplate.convertAndSend(EXCHANGE_NAME, INCIDENT_CREATED_ROUTING_KEY, incidentCreatedMessage);


        return incidentMapper.toResponse(savedIncident);
    }


    @RabbitListener(queues = "ai-evaluation-queue")
    public void handleAiEvaluation(){
        System.out.println("received message from AI service");
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

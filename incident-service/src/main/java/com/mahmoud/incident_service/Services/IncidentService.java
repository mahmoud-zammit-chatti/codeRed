package com.mahmoud.incident_service.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahmoud.incident_service.Models.*;
import com.mahmoud.incident_service.Repositories.OutboxRepo;
import com.mahmoud.schemas.events.IncidentAiEvalEvent;

import com.mahmoud.incident_service.DTOs.CreateIncidentRequestDTO;
import com.mahmoud.incident_service.DTOs.IncidentMapper;
import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;
import com.mahmoud.schemas.events.IncidentCreatedEvent;


import com.mahmoud.incident_service.Repositories.IncidentRepo;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;
    @Value("${INCIDENT_CREATED_ROUTING_KEY}")
    String INCIDENT_CREATED_ROUTING_KEY;
    @Value("${AI_CLASSIFICATION_QUEUE}")
    String AI_CLASSIFICATION_QUEUE;

    private final IncidentRepo incidentRepo;
    private final IncidentMapper incidentMapper;
    private final AmqpTemplate amqpTemplate;
    private final Clock buisnessClock;
    private final OutboxRepo outboxRepo;
    private final ObjectMapper ObjectMapper = new ObjectMapper();
    public IncidentService(IncidentRepo incidentRepo, IncidentMapper incidentMapper, AmqpTemplate amqpTemplate, Clock buisnessClock, OutboxRepo outboxRepo) {
        this.incidentRepo = incidentRepo;
        this.incidentMapper = incidentMapper;
        this.amqpTemplate = amqpTemplate;
        this.buisnessClock = buisnessClock;
        this.outboxRepo = outboxRepo;
    }

    @Transactional
    public IncidentResponseDTO createIncident(CreateIncidentRequestDTO request){
        Incident incident = incidentMapper.toEntity(request);
        incident.setCreatedAt(Instant.now(buisnessClock));
        incident.setClassificationStatus(ClassificationStatus.PENDING_CLASSIFICATION);
        Incident savedIncident = incidentRepo.save(incident);
        //publish incident to RabbitMQ

        // build the payload for sending using shared DTO
        IncidentCreatedEvent incidentCreatedEvent = incidentMapper.toIncidentCreatedEvent(savedIncident);
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .title("Incident Created")
                    .payload(ObjectMapper.writeValueAsString(incidentCreatedEvent))
                    .createdAt(Instant.now(buisnessClock))
                    .sent(false)
                    .build();
            outboxRepo.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error while building outbox payload: "+e);
        }


        

        return incidentMapper.toResponse(savedIncident);
    }



    @Scheduled(fixedDelay = 500)
    public void relayOutboxEvents() {
        ObjectMapper mapper = new ObjectMapper();
        List<OutboxEvent> pending = outboxRepo.findBySentFalseOrderByCreatedAtAsc();
        for (OutboxEvent row : pending) {
            try {
                IncidentCreatedEvent event = mapper.readValue(row.getPayload(), IncidentCreatedEvent.class);
                amqpTemplate.convertAndSend(EXCHANGE_NAME, INCIDENT_CREATED_ROUTING_KEY,event);
                row.setSent(true);
                outboxRepo.save(row);
            } catch (Exception e) {
                // leave it unsent, it'll retry next tick — no data loss possible
            }
        }
    }




  @RabbitListener(queues = "incident.classified.queue")
    @Transactional
    public void handleAiEvaluation(IncidentAiEvalEvent event){
        System.out.println("received message from AI service");
        Incident incident = incidentRepo.findById(event.getIncidentId()).orElseThrow(()-> new IncidentNotFoundException("There was a problem finding the requested incident"));
        incident.setClassificationStatus(ClassificationStatus.CLASSIFIED);
        incident.setSeverity(Severity.valueOf(event.getEvaluation().toUpperCase()));
        incident.setType(Type.valueOf(event.getRecommendedAction().toUpperCase()));
        incident.setUpdatedAt(Instant.now(buisnessClock));
        incidentRepo.save(incident);


    }

    @Transactional
    public IncidentResponseDTO getIncidentById(UUID incidentId){
        Incident incident = incidentRepo.findById(incidentId).orElseThrow(()-> new IncidentNotFoundException("There was a problem finding the requested incident"));
        return incidentMapper.toResponse(incident);
    }

    @Transactional
    public void  setIncidentStatus(UUID incidentId, Status status){
        Incident incident = incidentRepo.findById(incidentId).orElseThrow(()-> new IncidentNotFoundException("There was a problem finding the requested incident"));
        incident.setStatus(status);
        incidentRepo.save(incident);
    }

    @Transactional
    public void setIncidentSeverity(UUID incidentId, Severity severity){
        Incident incident = incidentRepo.findById(incidentId).orElseThrow(()-> new IncidentNotFoundException("There was a problem finding the requested incident"));
        incident.setSeverity(severity);
        incidentRepo.save(incident);
    }

    @Transactional
    public ArrayList<IncidentResponseDTO> getAllPendingIncidents(){

        ArrayList<Incident> incidents = incidentRepo.findAllPendingIncidents();
        ArrayList<IncidentResponseDTO> incidentResponseDTOS = new ArrayList<>();
        for (Incident incident : incidents) {
            incidentResponseDTOS.add(incidentMapper.toResponse(incident));
        }
        return incidentResponseDTOS;
    }




}

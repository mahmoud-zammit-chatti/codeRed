package com.mahmoud.incident_service.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahmoud.incident_service.Exceptions.ActionNotAllowedException;
import com.mahmoud.incident_service.Models.*;
import com.mahmoud.incident_service.Models.classification.Classification;
import com.mahmoud.incident_service.Models.incident.*;
import com.mahmoud.incident_service.Repositories.ClassificationRepo;
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

@Service
public class IncidentService {

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;
    @Value("${INCIDENT_CREATED_ROUTING_KEY}")
    String INCIDENT_CREATED_ROUTING_KEY;
    @Value("${AI_CLASSIFICATION_QUEUE}")
    String AI_CLASSIFICATION_QUEUE;
    @Value("${MODEL_VERSION}")
    String MODEL_VERSION;

    private final IncidentRepo incidentRepo;
    private final IncidentMapper incidentMapper;
    private final AmqpTemplate amqpTemplate;
    private final Clock buisnessClock;
    private final OutboxRepo outboxRepo;
    private final ObjectMapper ObjectMapper = new ObjectMapper();
    private final ClassificationRepo classificationRepo;
    public IncidentService(IncidentRepo incidentRepo, IncidentMapper incidentMapper, AmqpTemplate amqpTemplate, Clock buisnessClock, OutboxRepo outboxRepo, ClassificationRepo classificationRepo) {
        this.incidentRepo = incidentRepo;
        this.incidentMapper = incidentMapper;
        this.amqpTemplate = amqpTemplate;
        this.buisnessClock = buisnessClock;
        this.outboxRepo = outboxRepo;
        this.classificationRepo = classificationRepo;
    }

    @Transactional
    public IncidentResponseDTO createIncident(CreateIncidentRequestDTO request){
        Incident incident = incidentMapper.toEntity(request);
        incident.setCreatedAt(Instant.now(buisnessClock));
        incident.setClassificationStatus(IncidentClassificationStatus.PENDING_CLASSIFICATION);
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
        try {
            Incident incident = incidentRepo.findById(event.getIncidentId()).orElseThrow(()-> new IncidentNotFoundException("There was a problem finding the requested incident"));

            if(incident.getStatus().equals(Status.RESOLVED)){
                throw new ActionNotAllowedException("This incident has already been resolved. You can't ask a classification for it.");
            }

          Classification classification = Classification.builder()
                  .classificationStatus(com.mahmoud.incident_service.Models.classification.ClassificationStatus.PENDING)
                  .incidentRef(incident)
                  .confidence(event.getConfidenceScore())
                  .modelVersion(MODEL_VERSION)
                  .suggestedType(Type.valueOf(event.getRecommendedAction()))
                  .suggestedSeverity(Severity.valueOf(event.getEvaluation()))
                  .createdAt(Instant.now(buisnessClock))
                  .updatedAt(null)
                  .build();
          classificationRepo.save(classification);
          incident.setClassificationStatus(IncidentClassificationStatus.PENDING_CONFIRMATION);
          incidentRepo.save(incident);
        } catch (IncidentNotFoundException | ActionNotAllowedException e) {
            System.err.println("Failed to process AI evaluation for incident " + event.getIncidentId() + ": " + e.getMessage());
            // Message is acknowledged, preventing infinite retry loop
        }
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
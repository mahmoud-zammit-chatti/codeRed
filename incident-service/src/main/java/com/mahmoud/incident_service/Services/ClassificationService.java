package com.mahmoud.incident_service.Services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahmoud.incident_service.DTOs.IncidentMapper;
import com.mahmoud.incident_service.DTOs.ManualEditClassificationDTO;
import com.mahmoud.incident_service.Exceptions.ActionNotAllowedException;
import com.mahmoud.incident_service.Exceptions.ClassificationNotFoundException;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;
import com.mahmoud.incident_service.Models.OutboxEvent;
import com.mahmoud.incident_service.Models.classification.Classification;
import com.mahmoud.incident_service.Models.classification.ClassificationStatus;
import com.mahmoud.incident_service.Models.classification.Trigger;
import com.mahmoud.incident_service.Models.incident.Incident;
import com.mahmoud.incident_service.Models.incident.IncidentClassificationStatus;
import com.mahmoud.incident_service.Models.incident.Severity;
import com.mahmoud.incident_service.Models.incident.Type;
import com.mahmoud.incident_service.Repositories.ClassificationRepo;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
import com.mahmoud.incident_service.Repositories.OutboxRepo;
import com.mahmoud.schemas.events.ClassificationRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClassificationService {
    private final ClassificationRepo classificationRepo;
    private final IncidentRepo incidentRepo;
    private final Clock clock ;
    private final IncidentMapper incidentMapper;
    private final OutboxRepo outboxRepo;
    private ObjectMapper ObjectMapper = new ObjectMapper();

    public ClassificationService(ClassificationRepo classificationRepo, IncidentRepo incidentRepo, Clock clock, IncidentMapper incidentMapper, OutboxRepo outboxRepo) {
        this.classificationRepo = classificationRepo;
        this.incidentRepo = incidentRepo;
        this.clock = clock;
        this.incidentMapper = incidentMapper;
        this.outboxRepo = outboxRepo;
    }

    public List<Classification> getAllPendingIncidentClassifications(UUID incidentId) {
        return classificationRepo.findAllByIncidentRef_IdAndClassificationStatusOrderByCreatedAtDesc(incidentId, ClassificationStatus.PENDING);
    }

    @Transactional
    public Classification confirmIncidentClassification(UUID classificationId) {

        Classification classification = classificationRepo.findById(classificationId).orElseThrow(()->new ClassificationNotFoundException("Classification not found with id: " + classificationId));

        if(classification.getClassificationStatus() != ClassificationStatus.PENDING){
            throw new ActionNotAllowedException("Classification with id: " + classificationId + " is not in PENDING status and cannot be confirmed.");
        }

        classification.setClassificationStatus(ClassificationStatus.CONFIRMED);
        classification.setUpdatedAt(Instant.now(clock));
        Incident incident = incidentRepo.findById(classification.getIncidentRef().getId()).orElseThrow(()->new IncidentNotFoundException("Incident not found with id: " + classification.getIncidentRef().getId()));
        incident.setSeverity(classification.getSuggestedSeverity());
        incident.setType(classification.getSuggestedType());
        incident.setClassificationStatus(IncidentClassificationStatus.CLASSIFIED);
        incident.setUpdatedAt(Instant.now(clock));
        classificationRepo.save(classification);
        incidentRepo.save(incident);
        return classification;
    }

    @Transactional
    public Classification editAndConfirmClassification(UUID classificationId, ManualEditClassificationDTO request){
        Classification classification = classificationRepo.findById(classificationId).orElseThrow(()->new ClassificationNotFoundException("Classification not found with id: " + classificationId));
        Incident incident = incidentRepo.findById(classification.getIncidentRef().getId()).orElseThrow(()->new IncidentNotFoundException("Incident not found with id: " + classification.getIncidentRef().getId()));

        if(!request.getSuggestedSeverity().isBlank()) {
            classification.setSuggestedSeverity(Severity.valueOf(request.getSuggestedSeverity()));
        }
        if(!request.getSuggestedType().isBlank()) {
            classification.setSuggestedType(Type.valueOf( request.getSuggestedType()));
        }
        classification.setUpdatedAt(Instant.now(clock));
        classification.setClassificationStatus(ClassificationStatus.EDITEDANDCONFIRMED);
        incident.setClassificationStatus(IncidentClassificationStatus.CLASSIFIED);
        incident.setUpdatedAt(Instant.now(clock));
        incidentRepo.save(incident);
        classificationRepo.save(classification);

        return confirmIncidentClassification(classificationId);
    }

    public void requestManualClassification(UUID incidentId){

        Incident savedIncident = incidentRepo.findById(incidentId).orElseThrow(()->new IncidentNotFoundException("Incident not found with id: " + incidentId));

        ClassificationRequest ClassificationRequest = incidentMapper.toClassificationRequest(savedIncident);
        String correlationId = MDC.get("correlationId");
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .title("Incident Created")
                    .payload(ObjectMapper.writeValueAsString(ClassificationRequest))
                    .createdAt(Instant.now(clock))
                    .correlationId(correlationId)
                    .incidentRef(savedIncident)
                    .sent(false)
                    .trigger(Trigger.MANUAL)
                    .build();
            outboxRepo.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error while building outbox payload: "+e);
        }



    }


}
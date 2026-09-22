package com.mahmoud.incident_service.Services;


import com.mahmoud.incident_service.DTOs.ManualEditClassificationDTO;
import com.mahmoud.incident_service.Exceptions.ActionNotAllowedException;
import com.mahmoud.incident_service.Exceptions.ClassificationNotFoundException;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;
import com.mahmoud.incident_service.Models.classification.Classification;
import com.mahmoud.incident_service.Models.classification.ClassificationStatus;
import com.mahmoud.incident_service.Models.incident.Incident;
import com.mahmoud.incident_service.Models.incident.IncidentClassificationStatus;
import com.mahmoud.incident_service.Models.incident.Severity;
import com.mahmoud.incident_service.Models.incident.Type;
import com.mahmoud.incident_service.Repositories.ClassificationRepo;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
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

    public ClassificationService(ClassificationRepo classificationRepo, IncidentRepo incidentRepo, Clock clock) {
        this.classificationRepo = classificationRepo;
        this.incidentRepo = incidentRepo;
        this.clock = clock;
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

}
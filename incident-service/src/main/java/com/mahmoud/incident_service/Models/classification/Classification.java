package com.mahmoud.incident_service.Models.classification;

import com.mahmoud.incident_service.Models.incident.Incident;
import com.mahmoud.incident_service.Models.incident.Severity;
import com.mahmoud.incident_service.Models.incident.Type;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Classification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "incident_id", nullable = false)
    private Incident incidentRef;

    @Enumerated(EnumType.STRING)
    private Severity suggestedSeverity;
    @Enumerated(EnumType.STRING)
    private Type suggestedType;

    private Integer confidence;

    private double modelVersion; // version of the model used for classification

    @Enumerated(EnumType.STRING)
    private ClassificationStatus classificationStatus;

    private Instant createdAt;

    private Instant updatedAt;
}
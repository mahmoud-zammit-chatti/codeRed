package com.mahmoud.incident_service.Models.classification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mahmoud.incident_service.Models.incident.Incident;
import com.mahmoud.incident_service.Models.incident.Severity;
import com.mahmoud.incident_service.Models.incident.Type;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "incident_id", nullable = false)
    @JsonIgnore
    private Incident incidentRef;

    @Enumerated(EnumType.STRING)
    private Severity suggestedSeverity;
    @Enumerated(EnumType.STRING)
    private Type suggestedType;


    private String confirmedBy; // agent who confirmed the classification

    @Builder.Default
    private boolean edited = false;

    private Integer confidence;

    @Enumerated(EnumType.STRING)
    private Trigger trigger;


    private String modelVersion; // version of the model used for classification

    @Enumerated(EnumType.STRING)
    private ClassificationStatus classificationStatus;

    private Instant createdAt;

    private Instant updatedAt;
}

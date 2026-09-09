package com.mahmoud.incident_service.DTOs;

import com.mahmoud.incident_service.Models.incident.Severity;
import com.mahmoud.incident_service.Models.incident.Status;
import com.mahmoud.incident_service.Models.incident.Type;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class IncidentResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private String location;
    private Type type;
    private Status status;
    private Severity severity;
    private UUID agentId;
    private UUID dispatchedUnitId;
    private String reportedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
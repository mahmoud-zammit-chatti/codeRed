package com.mahmoud.incident_service.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

import com.mahmoud.incident_service.Models.*;
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

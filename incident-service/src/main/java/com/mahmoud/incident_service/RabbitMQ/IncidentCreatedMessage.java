package com.mahmoud.incident_service.RabbitMQ;


import com.mahmoud.incident_service.Models.Severity;
import com.mahmoud.incident_service.Models.Status;
import com.mahmoud.incident_service.Models.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentCreatedMessage {

    public UUID incidentId;
    public String title;
    public String description;
    public String location;
    public String reportedBy;
    public Type type;
    public Status status;
    public Severity severity;
    public UUID agentId;
    public UUID dispatchedUnitId;
    public Instant createdAt;
    public Instant updatedAt;


}

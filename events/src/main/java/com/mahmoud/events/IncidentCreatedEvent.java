package com.mahmoud.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;


public class IncidentCreatedEvent {

    private UUID id;
    private String title;
    private String description;
    private String location;
    private String type;
    private String severity;
    private String reportedBy;
    private Instant createdAt;

    public IncidentCreatedEvent() { }

    @JsonCreator
    public IncidentCreatedEvent(
            @JsonProperty("id") UUID id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("location") String location,
            @JsonProperty("type") String type,
            @JsonProperty("severity") String severity,
            @JsonProperty("reportedBy") String reportedBy,
            @JsonProperty("createdAt") Instant createdAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.severity = severity;
        this.reportedBy = reportedBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
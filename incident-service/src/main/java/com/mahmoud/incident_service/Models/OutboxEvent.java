package com.mahmoud.incident_service.Models;


import com.mahmoud.incident_service.Models.incident.Incident;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String payload;

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_ref")
    private Incident incidentRef;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "sent")
    private boolean sent;

}
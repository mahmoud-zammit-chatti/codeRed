package com.mahmoud.incident_service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahmoud.incident_service.DTOs.IncidentMapper;
import com.mahmoud.incident_service.Models.OutboxEvent;
import com.mahmoud.incident_service.Models.classification.Classification;
import com.mahmoud.incident_service.Models.classification.ClassificationStatus;
import com.mahmoud.incident_service.Models.incident.*;
import com.mahmoud.incident_service.Repositories.ClassificationRepo;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
import com.mahmoud.incident_service.Repositories.OutboxRepo;
import com.mahmoud.incident_service.Services.IncidentService;
import com.mahmoud.schemas.events.IncidentAiEvalEvent;
import com.mahmoud.schemas.events.IncidentCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {

    @Mock
    private IncidentRepo incidentRepo;

    @Mock
    Message mockMessage;
    @Mock
    private MessageProperties mockProperties;

    @Mock
    private ClassificationRepo classificationRepo;
    @Mock
    private  IncidentMapper incidentMapper;
    @Mock
    private  AmqpTemplate amqpTemplate;

    private  Clock buisnessClock;
    @Mock
    private  OutboxRepo outboxRepo;

    private  ObjectMapper ObjectMapper = new ObjectMapper();

    private IncidentService incidentService;

    private Instant randomTime ;
    private Instant randomTime2 ;

    @BeforeEach
    void setUp() {
        this.buisnessClock = Clock.systemUTC();
         this.randomTime = Instant.now(buisnessClock);
         this.randomTime2 = randomTime.plusSeconds(180);
         this.incidentService = new IncidentService(incidentRepo,incidentMapper,amqpTemplate,buisnessClock,outboxRepo,classificationRepo);
    }


    @Test
    void handleClassificationWhenIncidentIsResolved() {
        Incident resolvedIncident = createIncident(Severity.HIGH, Type.OTHER, IncidentClassificationStatus.CLASSIFIED, Status.RESOLVED);


        IncidentAiEvalEvent event = new IncidentAiEvalEvent();
        event.setId(UUID.randomUUID());
        event.setIncidentId(resolvedIncident.getId());
        event.setEvaluation("HIGH");
        event.setRecommendedAction("OTHER");
        event.setConfidenceScore(100);

        when(incidentRepo.findById(resolvedIncident.getId())).thenReturn(Optional.of(resolvedIncident));


        when(mockMessage.getMessageProperties()).thenReturn(mockProperties);



        assertDoesNotThrow(()-> incidentService.handleAiEvaluation(event,mockMessage));
        verify(incidentRepo).findById(resolvedIncident.getId());
        verify(classificationRepo, never()).save(any());
        verify(incidentRepo, never()).save(
                argThat(i ->
                i.getClassificationStatus() == IncidentClassificationStatus.PENDING_CONFIRMATION));




    }

    @Test
    void handleClassificationWhenIncidentIsNotResolved() {
        Incident unresolvedIncident  = createIncident(Severity.HIGH, Type.OTHER, IncidentClassificationStatus.PENDING_CLASSIFICATION, Status.PENDING);


        when(incidentRepo.findById(unresolvedIncident.getId())).thenReturn(Optional.of(unresolvedIncident));
        when(mockMessage.getMessageProperties()).thenReturn(mockProperties);


        IncidentAiEvalEvent event = new IncidentAiEvalEvent();
        event.setId(UUID.randomUUID());
        event.setIncidentId(unresolvedIncident.getId());
        event.setEvaluation("HIGH");
        event.setRecommendedAction("OTHER");
        event.setConfidenceScore(100);

        Classification classification = Classification.builder()
                .id(UUID.randomUUID())
                .incidentRef(unresolvedIncident)
                .suggestedSeverity(Severity.HIGH)
                .suggestedType(Type.OTHER)
                .classificationStatus(ClassificationStatus.PENDING)
                .confidence(100)
                .modelVersion("test")
                .createdAt(randomTime)
                .updatedAt(randomTime2)
                .build();


        assertDoesNotThrow(()-> incidentService.handleAiEvaluation(event,mockMessage));

        verify(incidentRepo).findById(unresolvedIncident.getId());
        verify(classificationRepo).save(argThat(c->
                c.getIncidentRef().getId().equals(unresolvedIncident.getId())));
        verify(incidentRepo).save(
                argThat(i ->
                i.getClassificationStatus() == IncidentClassificationStatus.PENDING_CONFIRMATION));

    }


    @Test
    void relayOutboxEventsWhenSendingEvents() throws JsonProcessingException {

        ReflectionTestUtils.setField(incidentService, "EXCHANGE_NAME", "test.exchange");
        ReflectionTestUtils.setField(incidentService, "INCIDENT_CREATED_ROUTING_KEY", "incident.created");

        Incident pendingIncident = createIncident(Severity.MEDIUM, Type.OTHER, IncidentClassificationStatus.PENDING_CLASSIFICATION, Status.PENDING);
        IncidentCreatedEvent event = new IncidentCreatedEvent();
        event.setId(UUID.randomUUID());
        event.setTitle("test");
        event.setDescription("test");
        event.setLocation("test");
        event.setSeverity(pendingIncident.getSeverity().toString());
        event.setType(pendingIncident.getType().toString());
        event.setReportedBy("test");
        event.setCreatedAt(Date.from(pendingIncident.getCreatedAt()));

        OutboxEvent outboxEvent = OutboxEvent.builder()
                        .id(UUID.randomUUID())
                        .title("IncidentCreatedEvent")
                        .payload(ObjectMapper.writeValueAsString(event))
                        .createdAt(pendingIncident.getCreatedAt())
                        .sent(false)
                                .build();
        when(outboxRepo.findBySentFalseOrderByCreatedAtAsc()).thenReturn(java.util.List.of(outboxEvent));

        incidentService.relayOutboxEvents();
        verify(amqpTemplate).convertAndSend(eq("test.exchange"),eq("incident.created"), any(IncidentCreatedEvent.class),any(MessagePostProcessor.class));
        verify(outboxRepo).save(argThat(OutboxEvent::isSent));

    }

    private Incident createIncident(Severity severity, Type type, IncidentClassificationStatus classificationStatus, Status status) {
        return Incident.builder()
                .id(java.util.UUID.randomUUID())
                .severity(severity)
                .type(type)
                .classificationStatus(classificationStatus)
                .status(status)

                .createdAt(this.randomTime)
                .updatedAt(this.randomTime2)
                .build();
    }

}
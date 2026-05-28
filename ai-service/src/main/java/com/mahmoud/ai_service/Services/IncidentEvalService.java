package com.mahmoud.ai_service.Services;


import com.mahmoud.events.IncidentCreatedEvent;
import com.mahmoud.events.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.Thread.sleep;

@Service
public class IncidentEvalService {
    private final AmqpTemplate amqpTemplate;

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;
    @Value("${INCIDENT_EVALUATED_ROUTING_KEY}")
    String INCIDENT_EVALUATED_ROUTING_KEY;

    public IncidentEvalService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues = "incident-creation-queue")
    public void handleIncidentCreation(IncidentCreatedEvent event){

        System.out.println("AI Service received incident: " + event.getId());
        IncidentAiEvalEvent incidentAiEvalEvent = evaluateIncident(event.getId());

        amqpTemplate.convertAndSend(EXCHANGE_NAME, INCIDENT_EVALUATED_ROUTING_KEY, incidentAiEvalEvent);

    }

    private IncidentAiEvalEvent evaluateIncident(UUID incidentId){
        try {
            sleep(2000);
            IncidentAiEvalEvent incidentAiEvalEvent = new IncidentAiEvalEvent();
            incidentAiEvalEvent.setId(UUID.randomUUID());
            incidentAiEvalEvent.setIncidentId(incidentId);
            incidentAiEvalEvent.setEvaluation("High");
            incidentAiEvalEvent.setRecommendedAction("FIRE");
            incidentAiEvalEvent.setConfidenceScore(90);
        return incidentAiEvalEvent;
        }catch (InterruptedException e){
            e.printStackTrace();
        }



return null;
    }

}

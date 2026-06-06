package com.mahmoud.ai_service.Services;


import com.mahmoud.ai_service.Exceptions.IncidentEvaluationException;
import com.mahmoud.ai_service.Models.AiEval;
import com.mahmoud.ai_service.Repositories.AiEvalRepo;
import com.mahmoud.schemas.events.IncidentAiEvalEvent;
import com.mahmoud.schemas.events.IncidentCreatedEvent;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
public class IncidentEvalService {
    private final AmqpTemplate amqpTemplate;
    private final OpenAiChatModel model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiEvalRepo aiEvalRepo;

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;
    @Value("${INCIDENT_EVALUATED_ROUTING_KEY}")
    String INCIDENT_EVALUATED_ROUTING_KEY;

    public IncidentEvalService(AmqpTemplate amqpTemplate, OpenAiChatModel model, AiEvalRepo aiEvalRepo) {
        this.amqpTemplate = amqpTemplate;
        this.model = model;
        this.aiEvalRepo = aiEvalRepo;
    }

    @RabbitListener(queues = "incident-creation-queue")
    public void handleIncidentCreation(IncidentCreatedEvent event){

        System.out.println("AI Service received incident: " + event.getId());
       


            IncidentAiEvalEvent incidentAiEvalEvent = evaluateIncident(event.getId(),event.getDescription());
        

        amqpTemplate.convertAndSend(EXCHANGE_NAME, INCIDENT_EVALUATED_ROUTING_KEY, incidentAiEvalEvent);

    }

    @Transactional
    private IncidentAiEvalEvent evaluateIncident(UUID id,String description) throws IncidentEvaluationException {
        // Build a clear, strict prompt that asks the model to return only a JSON object
        // with the exact fields we expect. Use String.format to safely inject values.
        String safeDescription = description == null ? "" : description.replace("\"", "'");
        String prompt = String.format("""
                You are an automated incident triage assistant. Evaluate the incident below and RETURN ONLY a single JSON object (no surrounding text, no markdown):

                REQUIRED JSON OBJECT FIELDS:
                {
                  "id": "<a new UUID string>",
                  "incidentId": "<the original incident UUID provided below>",
                  "evaluation": "<severity: LOW | MEDIUM | HIGH | CRITICAL>",
                  "recommendedAction": "<type: MEDICAL | FIRE | CRIME | ACCIDENT | OTHER>",
                  "confidenceScore": <integer 0-100>
                }

                RULES:
                - 'incidentId' MUST exactly match the provided incident id.
                - 'evaluation' must be one of: LOW, MEDIUM, HIGH, CRITICAL (exact casing).
                - 'recommendedAction' must be one of: MEDICAL, FIRE, CRIME, ACCIDENT, OTHER (exact casing).
                - 'confidenceScore' must be an integer between 0 and 100 (no percent sign).
                - Do not include any explanatory text, headings, or markdown — output raw JSON only.

                INCIDENT TO EVALUATE:
                id: %s
                description: %s

                EXAMPLE RESPONSE (format only):
                {"id":"11111111-1111-1111-1111-111111111111","incidentId":"%s","evaluation":"HIGH","recommendedAction":"FIRE","confidenceScore":87}
                """, id.toString(), safeDescription, id.toString());

                
        String response = model.call(prompt);
        if (response == null || response.isBlank()) {
            throw new IncidentEvaluationException("AI model returned an empty response for incident: " + id);
        }

        // Extract fields from the model response and map them into the event.
        IncidentAiEvalEvent event = new IncidentAiEvalEvent();
        event.setIncidentId(id);
        try {
            String json = extractJsonObject(response);
            JsonNode root = objectMapper.readTree(json);
            event.setEvaluation(textOrNull(root, "evaluation"));
            event.setRecommendedAction(textOrNull(root, "recommendedAction"));
            if (root.hasNonNull("confidenceScore")) {
                event.setConfidenceScore(root.get("confidenceScore").asInt());
            }
        } catch (Exception ex) {
            throw new IncidentEvaluationException("Failed to parse AI response JSON for incident: " + id, ex);
        }
        
        
        AiEval evaluation = AiEval.builder()
        .confidenceScore(event.getConfidenceScore())
        .evaluation(event.getEvaluation())
        .incidentId(event.getIncidentId())
        .recommandedAction(event.getRecommendedAction())
        .createdAt(Instant.now())
        .build();
        
        AiEval savedEval = aiEvalRepo.save(evaluation);

        event.setId(savedEval.getId());

        return event;
    }



    private String extractJsonObject(String response) {
        if (response == null) {
            return "{}";
        }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private String textOrNull(JsonNode root, String field) {
        JsonNode value = root.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }


}

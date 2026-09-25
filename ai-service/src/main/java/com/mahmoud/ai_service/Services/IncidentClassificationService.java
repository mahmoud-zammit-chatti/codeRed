package com.mahmoud.ai_service.Services;


import com.mahmoud.ai_service.Exceptions.IncidentEvaluationException;
import com.mahmoud.ai_service.Models.AiEval;
import com.mahmoud.ai_service.Repositories.AiEvalRepo;
import com.mahmoud.schemas.events.IncidentAiEvalEvent;
import com.mahmoud.schemas.events.ClassificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class IncidentClassificationService {
    private final AmqpTemplate amqpTemplate;
    private final OpenAiChatModel model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiEvalRepo aiEvalRepo;

    @Value("${EXCHANGE_NAME}")
    String EXCHANGE_NAME;
    @Value("${INCIDENT_EVALUATED_ROUTING_KEY}")
    String INCIDENT_CLASSIFIED_ROUTING_KEY;

    public IncidentClassificationService(AmqpTemplate amqpTemplate, OpenAiChatModel model, AiEvalRepo aiEvalRepo) {
        this.amqpTemplate = amqpTemplate;
        this.model = model;
        this.aiEvalRepo = aiEvalRepo;
    }





    @RabbitListener(queues = "classification.requested.queue")
    public void handleIncidentCreation(ClassificationRequest event, Message message) {

        String correlationId = message.getMessageProperties().getHeader("x-correlation-id");
        String resolvedCorrelationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        MDC.put("correlationId", resolvedCorrelationId);

        log.info("AI Service received incident: {} with correlationId {}", event.getId(), resolvedCorrelationId);
        try{

        IncidentAiEvalEvent incidentAiEvalEvent = evaluateIncident(event.getId(),event.getDescription());
            log.info("trigger type of incident: {}", event.getTrigger());
        incidentAiEvalEvent.setTrigger(event.getTrigger());

        MessagePostProcessor addCorrelationHeader = outboundMessage -> {
            outboundMessage.getMessageProperties().setHeader("x-correlation-id", resolvedCorrelationId);
            return outboundMessage;
        };

        amqpTemplate.convertAndSend(EXCHANGE_NAME, INCIDENT_CLASSIFIED_ROUTING_KEY, incidentAiEvalEvent, addCorrelationHeader);
        }catch (IncidentEvaluationException e){
            log.warn("Error evaluating  incident with correlationId {}: {}",resolvedCorrelationId, e.getMessage());
            throw new AmqpRejectAndDontRequeueException("incident sent to dlx");
        }finally {
            MDC.clear();
        }


    }

    @Transactional
    public IncidentAiEvalEvent evaluateIncident(UUID id,String description) throws IncidentEvaluationException {
        // Build a clear, strict prompt that asks the model to return only a JSON object
        // with the exact fields we expect. Use String.format to safely inject values.
        String safeDescription = description == null ? "" : description.replace("\"", "'");
        String prompt = String.format("""
                You are an automated incident classification assistant. Evaluate the incident below and RETURN ONLY a single JSON object (no surrounding text, no markdown):

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
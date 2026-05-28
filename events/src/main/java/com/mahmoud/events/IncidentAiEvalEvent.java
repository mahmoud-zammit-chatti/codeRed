package com.mahmoud.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.UUID;


public class IncidentAiEvalEvent {

    private UUID id;
    private UUID incidentId;
    private String evaluation;
    private String recommendedAction;
    private Integer confidenceScore;

    public IncidentAiEvalEvent() { }

    @JsonCreator
    public IncidentAiEvalEvent(
            @JsonProperty("id") UUID id,
            @JsonProperty("incidentId") UUID incidentId,
            @JsonProperty("evaluation") String evaluation,
            @JsonProperty("recommendedAction") String recommendedAction,
            @JsonProperty("confidenceScore") Integer confidenceScore
    ) {
        this.id = id;
        this.incidentId = incidentId;
        this.evaluation = evaluation;
        this.recommendedAction = recommendedAction;
        this.confidenceScore = confidenceScore;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }

    public String getEvaluation() { return evaluation; }
    public void setEvaluation(String evaluation) { this.evaluation = evaluation; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

}
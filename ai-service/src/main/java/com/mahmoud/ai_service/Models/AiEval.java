package com.mahmoud.ai_service.Models;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
public class AiEval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID incidentId;

    @NotBlank
    private String evaluation;

    @NotBlank
    private String recommandedAction;

    @Positive
    private int confidenceScore;

    private Instant createdAt;

}

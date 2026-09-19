package com.mahmoud.incident_service.DTOs;


import com.mahmoud.incident_service.Models.incident.Severity;
import com.mahmoud.incident_service.Models.incident.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualEditClassificationDTO {

    private String suggestedSeverity;
    private String suggestedType;


}
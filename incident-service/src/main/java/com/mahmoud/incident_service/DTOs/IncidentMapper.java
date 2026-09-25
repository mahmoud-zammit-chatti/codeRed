package com.mahmoud.incident_service.DTOs;


import com.mahmoud.incident_service.Models.incident.Incident;
import com.mahmoud.schemas.events.ClassificationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "severity", constant = "MEDIUM")
    @Mapping(target = "type", constant = "OTHER")
    @Mapping(target = "agentId", ignore = true)
    @Mapping(target= "dispatchedUnitId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Incident toEntity(CreateIncidentRequestDTO request);

    IncidentResponseDTO toResponse(Incident incident);

    ClassificationRequest toClassificationRequest(Incident incident);

}
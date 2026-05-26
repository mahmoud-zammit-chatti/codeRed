package com.mahmoud.incident_service.DTOs;


import com.mahmoud.incident_service.Models.Incident;
import com.mahmoud.incident_service.Models.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "severity", constant = "LOW")
    @Mapping(target = "type", constant = "OTHER")
    @Mapping(target = "agentId", ignore = true)
    @Mapping(target= "dispatchedUnitId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Incident toEntity(CreateIncidentRequestDTO request);

    IncidentResponseDTO toResponse(Incident incident);

}

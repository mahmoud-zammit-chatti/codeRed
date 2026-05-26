package com.mahmoud.incident_service.Exceptions;

import com.mahmoud.incident_service.DTOs.IncidentResponseDTO;

public class IncidentNotFoundException extends RuntimeException{

     public IncidentNotFoundException(String message) {
        super(message);
    }

}

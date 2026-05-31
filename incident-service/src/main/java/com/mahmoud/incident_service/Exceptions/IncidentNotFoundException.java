package com.mahmoud.incident_service.Exceptions;

public class IncidentNotFoundException extends RuntimeException{

     public IncidentNotFoundException(String message) {
        super(message);
    }

}

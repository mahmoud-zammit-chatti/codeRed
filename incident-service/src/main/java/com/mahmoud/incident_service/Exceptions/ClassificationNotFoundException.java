package com.mahmoud.incident_service.Exceptions;

public class ClassificationNotFoundException extends RuntimeException {
    public ClassificationNotFoundException(String message) {
        super(message);
    }
}
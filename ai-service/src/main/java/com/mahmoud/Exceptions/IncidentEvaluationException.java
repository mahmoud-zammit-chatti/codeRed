package com.mahmoud.Exceptions;

public class IncidentEvaluationException extends RuntimeException {
    public IncidentEvaluationException(String message) {
        super(message);
    }

    public IncidentEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

}

package com.mahmoud.incident_service.Config;


import java.time.Instant;
import java.util.ArrayList;

import com.mahmoud.incident_service.Exceptions.ActionNotAllowedException;
import com.mahmoud.incident_service.Exceptions.ClassificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mahmoud.incident_service.Exceptions.ApiError;
import com.mahmoud.incident_service.Exceptions.IncidentNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ApiError> handleAIncidentNotFoundException(IncidentNotFoundException ex) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .errorLabel(HttpStatus.NOT_FOUND.name())
                .details(new ArrayList<>())
                .build();
        
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<ApiError> handleActionNotAllowedException(ActionNotAllowedException ex) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .errorCode(HttpStatus.NOT_ACCEPTABLE.value())
                .errorLabel(HttpStatus.NOT_ACCEPTABLE.name())
                .details(new ArrayList<>())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(apiError);
    }

    @ExceptionHandler(ClassificationNotFoundException.class)
    public ResponseEntity<ApiError> handleClassificationNotFoundException(ClassificationNotFoundException ex) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .errorLabel(HttpStatus.NOT_FOUND.name())
                .details(new ArrayList<>())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

}
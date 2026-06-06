package com.mahmoud.Exceptions;

import java.time.Instant;
import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(IncidentEvaluationException.class)
    public ResponseEntity<ApiError> handleIncidentEvaluationException(IncidentEvaluationException ex) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .message(ex.getMessage())
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorLabel(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

}

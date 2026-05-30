package com.mahmoud.incident_service.Exceptions;

import java.time.Instant;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    
    Instant timestamp;
    String message;
    int errorCode;
    String errorLabel;
    ArrayList<ErrorDetails> details;



}

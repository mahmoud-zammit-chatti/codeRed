package com.mahmoud.ai_service.Exceptions;

import java.time.Instant;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiError {
    
    Instant timestamp;
    String message;
    int errorCode;
    String errorLabel;
    ArrayList<ErrorDetails> details;



}

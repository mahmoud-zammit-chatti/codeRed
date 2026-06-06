package com.mahmoud.ai_service.Exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetails {

    private  String field;
    private  String message;

    

}

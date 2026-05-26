package com.mahmoud.incident_service.Config;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex) {
        // Log the exception (you can use a logging framework like Log4j or SLF4J)
        System.err.println("An error occurred: " + ex.getMessage());
        // Return a generic error message to the client
        return "An unexpected error occurred. Please try again later.";
    }
}

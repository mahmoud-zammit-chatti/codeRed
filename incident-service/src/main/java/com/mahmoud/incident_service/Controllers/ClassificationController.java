package com.mahmoud.incident_service.Controllers;


import java.util.List;
import java.util.UUID;

import com.mahmoud.incident_service.DTOs.ManualEditClassificationDTO;
import com.mahmoud.incident_service.Services.ClassificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.mahmoud.incident_service.Models.classification.Classification;

@RestController
@RequestMapping("/api/classification")
public class ClassificationController {
    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/protected/get-all-pending-classification")
    @SecurityRequirement(name = "Bearer Authentication")

    public ResponseEntity<List<Classification>> getAllPendingClassification(@RequestParam("incidentId") String incidentId) {
        return ResponseEntity.ok(classificationService.getAllPendingIncidentClassifications(UUID.fromString(incidentId)));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/protected/confirm-classification")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Classification> confirmClassification(@RequestParam("classificationId") String classificationId, @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(classificationService.confirmIncidentClassification(UUID.fromString(classificationId),jwt));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PutMapping("/protected/edit-confirm-classification")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Classification> editClassification(@RequestParam("classificationId") String classificationId, @RequestBody ManualEditClassificationDTO request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(classificationService.editAndConfirmClassification(UUID.fromString(classificationId), request, jwt));
    }

    @PreAuthorize("hasRole('AGENT')")
    @PostMapping("/protected/request-classification")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> requestClassification(@RequestParam("incidentId") String incidentId, @AuthenticationPrincipal Jwt jwt) {
        classificationService.requestManualClassification(UUID.fromString(incidentId),jwt);
        return ResponseEntity.ok().build();
    }

}
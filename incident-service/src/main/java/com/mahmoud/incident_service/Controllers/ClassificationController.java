package com.mahmoud.incident_service.Controllers;


import java.util.List;
import java.util.UUID;

import com.mahmoud.incident_service.DTOs.ManualEditClassificationDTO;
import com.mahmoud.incident_service.Services.ClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mahmoud.incident_service.Models.classification.Classification;

@RestController
@RequestMapping("/api/classification")
public class ClassificationController {
    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping("/get-all-pending-classification")
    public ResponseEntity<List<Classification>> getAllPendingClassification(@RequestParam("incidentId") String incidentId) {
        return ResponseEntity.ok(classificationService.getAllPendingIncidentClassifications(UUID.fromString(incidentId)));
    }

    @PostMapping("/confirm-classification")
    public ResponseEntity<Classification> confirmClassification(@RequestParam("classificationId") String classificationId) {

        return ResponseEntity.ok(classificationService.confirmIncidentClassification(UUID.fromString(classificationId)));
    }

    @PutMapping("/edit-confirm-classification")
    public ResponseEntity<Classification> editClassification(@RequestParam("classificationId") String classificationId, @RequestBody ManualEditClassificationDTO request) {
        return ResponseEntity.ok(classificationService.editAndConfirmClassification(UUID.fromString(classificationId), request));
    }

    @PostMapping("/request-classification")
    public ResponseEntity<Void> requestClassification(@RequestParam("incidentId") String incidentId) {
        classificationService.requestManualClassification(UUID.fromString(incidentId));
        return ResponseEntity.ok().build();
    }

}
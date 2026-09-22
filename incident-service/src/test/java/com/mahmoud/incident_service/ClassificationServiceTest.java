package com.mahmoud.incident_service;


import com.mahmoud.incident_service.Exceptions.ActionNotAllowedException;
import com.mahmoud.incident_service.Exceptions.ClassificationNotFoundException;
import com.mahmoud.incident_service.Models.classification.Classification;
import com.mahmoud.incident_service.Models.classification.ClassificationStatus;
import com.mahmoud.incident_service.Models.incident.Incident;
import com.mahmoud.incident_service.Models.incident.IncidentClassificationStatus;
import com.mahmoud.incident_service.Repositories.ClassificationRepo;
import com.mahmoud.incident_service.Repositories.IncidentRepo;
import com.mahmoud.incident_service.Services.ClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ClassificationServiceTest {


    @Mock
    private  ClassificationRepo classificationRepo;
    @Mock
    private  IncidentRepo incidentRepo;

    private  Clock clock ;

    private ClassificationService classificationService;

    @BeforeEach
    void setUp() {
        this.clock = Clock.systemUTC();
        this.classificationService = new ClassificationService(classificationRepo,incidentRepo,clock);
    }


    @Test
    void confirmIncidentClassificationWhenClassificationIsNotFound() {


        UUID classificationId = UUID.randomUUID();
        when(classificationRepo.findById(classificationId)).thenReturn(Optional.empty());
        assertThrows(ClassificationNotFoundException.class, ()->classificationService.confirmIncidentClassification(classificationId));

        verify(classificationRepo).findById(classificationId);
        verify(classificationRepo, never()).save(any());
        verify(incidentRepo, never()).save(any());
    }

    @Test
    void confirmIncidentClassificationWhenClassificationIsNotPending() {
        UUID classificationId = UUID.randomUUID();
        Classification classification = Classification.builder().id(classificationId).classificationStatus(ClassificationStatus.CONFIRMED).build();
        when(classificationRepo.findById(classificationId)).thenReturn(Optional.of(classification));


        assertThrows(ActionNotAllowedException.class, ()->classificationService.confirmIncidentClassification(classificationId));
        verify(classificationRepo).findById(classificationId);
        verify(classificationRepo, never()).save(any());
        verify(incidentRepo, never()).save(any());
    }

    @Test
    void confirmIncidentClassificationWhenClassificationIsPending() {
        UUID classificationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        Incident incident = Incident.builder().id(incidentId).build();

        Classification classification = Classification.builder().id(classificationId).incidentRef(incident).classificationStatus(ClassificationStatus.PENDING).build();
        when(classificationRepo.findById(classificationId)).thenReturn(Optional.of(classification));
        when(incidentRepo.findById(incidentId)).thenReturn(Optional.of(incident));

        classificationService.confirmIncidentClassification(classificationId);

        verify(classificationRepo).findById(classificationId);
        verify(incidentRepo).findById(incidentId);
        verify(classificationRepo).save(argThat(c -> c.getClassificationStatus() == ClassificationStatus.CONFIRMED));

        verify(incidentRepo).save(argThat(i->i.getClassificationStatus() == IncidentClassificationStatus.CLASSIFIED));
    }






}
package com.mahmoud.incident_service.Repositories;


import com.mahmoud.incident_service.Models.classification.Classification;
import com.mahmoud.incident_service.Models.classification.ClassificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassificationRepo extends JpaRepository<Classification, UUID> {


    @Override
    Optional<Classification> findById(UUID classificationId);
    List<Classification> findAllByIncidentRef_IdAndClassificationStatusOrderByCreatedAtDesc (UUID incidentId, ClassificationStatus status);
}
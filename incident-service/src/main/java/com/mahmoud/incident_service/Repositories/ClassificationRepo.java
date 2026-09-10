package com.mahmoud.incident_service.Repositories;


import com.mahmoud.incident_service.Models.classification.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassificationRepo extends JpaRepository<Classification, UUID> {


    Optional<Classification> findByIncidentRef_Id(UUID incidentId);
}
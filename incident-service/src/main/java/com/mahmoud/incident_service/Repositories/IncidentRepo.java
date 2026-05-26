package com.mahmoud.incident_service.Repositories;


import com.mahmoud.incident_service.Models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepo extends JpaRepository<Incident, UUID> {

    @Override
    Optional<Incident> findById(UUID incidentId);

    @Query("""
    SELECT i
    FROM Incident i
    WHERE i.status = 'PENDING'
    ORDER BY CASE i.severity
        WHEN 'CRITICAL' THEN 0
        WHEN 'HIGH' THEN 1
        WHEN 'MEDIUM' THEN 2
        WHEN 'LOW' THEN 3
        ELSE 4
    END
""")
ArrayList<Incident> findAllPendingIncidents();
}

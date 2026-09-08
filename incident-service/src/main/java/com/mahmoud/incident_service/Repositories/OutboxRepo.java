package com.mahmoud.incident_service.Repositories;


import com.mahmoud.incident_service.Models.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepo extends JpaRepository<OutboxEvent, UUID> {


    List<OutboxEvent> findBySentFalseOrderByCreatedAtAsc();

}

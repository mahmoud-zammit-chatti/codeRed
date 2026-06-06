package com.mahmoud.ai_service.Repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mahmoud.ai_service.Models.AiEval;

public interface AiEvalRepo extends JpaRepository<AiEval,UUID>{

}

package com.mahmoud.ai_service.Controllers;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-eval")
public class AiEvaluationController {

    @PostMapping("/evaluate")
    public void evaluateIncident() {
        // This endpoint is just a placeholder. The actual evaluation logic is handled in the IncidentEval
    }
}

package com.mahmoud.ai_service.GroqConfig;


import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service

public class GroqChatService {

    private final OpenAiChatModel model;

    public GroqChatService(OpenAiChatModel model) {
        this.model = model;
    }
    public String chat(String prompt) {
        return model.call(prompt);
    }

    public ChatOptions getChatOptions() {
        return model.getDefaultOptions();
    }
}

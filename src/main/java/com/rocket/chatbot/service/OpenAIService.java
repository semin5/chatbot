package com.rocket.chatbot.service;

import com.rocket.chatbot.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final String apiKey;

    public OpenAIService(
            WebClient webClient,
            @Value("${openai.secret-key}") String apiKey
    ) {
        this.webClient = webClient;
        this.apiKey = apiKey;
    }

    public Flux<String> chatStream(String message, Long conversationId) {

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "gpt-3.5-turbo",
                        "messages", List.of(Map.of("role", "user", "content", message)),
                        "stream", true
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .map(chunk -> {
                    // SSE 형식으로 변환
                    return "data: " + chunk + "\n\n";
                });
    }

    public String chat(List<Message> contextMessages){

        return null;
    }


}

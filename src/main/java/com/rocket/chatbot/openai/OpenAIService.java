package com.rocket.chatbot.openai;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    public Flux<String> chatStream(String message, Long conversationId) {
        // WebClient를 사용한 스트리밍 구현
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
}

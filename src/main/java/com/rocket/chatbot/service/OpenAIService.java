package com.rocket.chatbot.service;

import com.rocket.chatbot.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final String apiKey;

    public OpenAIService(
            WebClient webClient,
            @Value("${openai.api-key}") String apiKey
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

        List<Map<String, String>> messages = contextMessages.stream()
                .filter(m -> m.getRole() != null && m.getContent() != null)
                .map(m -> Map.of(
                        "role", m.getRole(),
                        "content", m.getContent()
                ))
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", messages,
                "stream", false
        );

        Map response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            throw new IllegalStateException("OpenAI response is null");
        }

        List choices = (List) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("OpenAI choices is empty");
        }

        Map firstChoice = (Map) choices.get(0);

        Map message = (Map) firstChoice.get("message");
        if (message == null) {
            throw new IllegalStateException("OpenAI message is missing");
        }

        Object content = message.get("content");
        if (content == null) {
            throw new IllegalStateException("OpenAI content is null");
        }

        return content.toString();
    }


}

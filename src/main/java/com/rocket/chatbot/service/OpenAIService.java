package com.rocket.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocket.chatbot.config.WebClientConfig;
import com.rocket.chatbot.domain.Message;
import com.rocket.chatbot.dto.*;
import com.rocket.chatbot.exception.BusinessException;
import com.rocket.chatbot.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final WebClientConfig webClientConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public Flux<String> chatStream(String message, Long conversationId) {

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
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

    public String chat(List<Message> message){

        try{
            List<Message> openAiMessages = message.stream()
                    .filter(m -> m.getRole() != null && m.getContent() != null)
                    .map(m -> new Message(m.getRole(), m.getContent()))
                    .toList();

            Chatbody body = Chatbody.builder()
                    .model(webClientConfig.getModel())
                    .messages(openAiMessages)
                    .stream(false)
                    .build();

            ChatResponse response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();

            ExResponse(response);
            return response.getChoices().get(0).getMessage().getContent();

        } catch (WebClientResponseException e){
            if (e.getStatusCode().value() == 429) {
                throw new BusinessException(ErrorCode.RATE_LIMITED, "OpenAI 요청이 너무 많습니다.", "rate_limited");
            }
            log.error("OpenAI error: status={}, body={}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "OpenAI 호출 실패", "external api error");

        } catch (Exception e){
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "OpenAI 통신 오류", "external api error");
        }
    }

    private void ExResponse(ChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "OpenAI API 응답이 비어있음", "external api error");
        }

        ChatResponse.Choice choice = response.getChoices().get(0);
        if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "OpenAI API 응답 형식이 올바르지 않음", "external api error");
        }
    }

    public Flux<String> ChatStream(List<OpenAIMessage> message) {

        List<OpenAIMessage> openAiMessages = message.stream()
                .filter(m -> m.getRole() != null && m.getContent() != null)
                .map(m -> new OpenAIMessage(m.getRole(), m.getContent()))
                .toList();

        ChatDto body = ChatDto.builder()
                .model(webClientConfig.getModel())
                .messages(openAiMessages)
                .stream(true)
                .build();

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(responseBody -> Flux.fromArray(responseBody.split("\n"))) // 줄 단위 분할
                .map(line -> {
                    if (line.startsWith("data:")) {
                        return line.substring(5).trim();
                    }
                    return line.trim();
                })
                .filter(data -> !data.isEmpty() && !"[DONE]".equals(data)) // [DONE] 및 빈 줄 필터링
                .map(data -> {
                    try {
                        ChatChunkResponse chunk =
                                objectMapper.readValue(data, ChatChunkResponse.class);
                        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                            String content = chunk.getChoices().get(0).getDelta().getContent();
                            return content != null ? content : "";
                        }
                        return "";
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                })
                .filter(content -> !content.isEmpty());
    }
}

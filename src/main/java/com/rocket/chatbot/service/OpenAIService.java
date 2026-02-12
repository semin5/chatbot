package com.rocket.chatbot.service;

import com.rocket.chatbot.domain.Message;
import com.rocket.chatbot.exception.BusinessException;
import com.rocket.chatbot.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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

    public String chat(List<Message> message){

        List<Map<String, String>> messages = message.stream()
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

        final Map response;
        try{
            response = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e){

            int code = e.getStatusCode().value();

            if (code == 429) {
                throw new BusinessException(
                        ErrorCode.RATE_LIMITED,
                        "OpenAI 요청이 너무 많습니다.",
                        "rate_limited"
                );
            }

            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "OpenAI 호출 실패",
                    "external_api_error"
            );

        } catch (Exception e){

            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "OpenAI 통신 오류",
                    "external api error"
            );
        }


        if (response == null) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "OpenAI 응답이 비어있음",
                    "external api error"
            );
        }

        List choices = (List) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "OpenAI 응답 형식이 올바르지 않음. choices 없음",
                    "external api error"
            );
        }

        Map firstChoice = (Map) choices.get(0);

        Map mes = (Map) firstChoice.get("message");
        if (mes == null) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "OpenAI 응답 형식이 올바르지 않음. message 없음",
                    "external api error"
            );
        }

        Object content = mes.get("content");
        if (content == null) {
            throw new BusinessException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "OpenAI 응답 형식이 올바르지 않음. content 없음",
                    "external api error"
            );
        }

        return content.toString();
    }


}

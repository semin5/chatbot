package com.rocket.chatbot.controller;

import com.rocket.chatbot.service.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OpenAIController {

    private final OpenAIService openAIService;

    @Operation(summary = "스트리밍 테스트")
    @GetMapping(value = "/chat/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String message,
                                   @RequestParam(required = false) Long conversationId) {
        return openAIService.chatStream(message, conversationId);
    }


}

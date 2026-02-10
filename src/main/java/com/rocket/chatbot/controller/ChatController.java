package com.rocket.chatbot.controller;

import com.rocket.chatbot.dto.ChatRequest;
import com.rocket.chatbot.dto.ConversationDto;
import com.rocket.chatbot.dto.MessageDto;
import com.rocket.chatbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import com.rocket.chatbot.dto.ChatResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "채팅 메시지 전송", description = "사용자 메시지를 전송하고 AI 응답을 받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/chat/completions")
    public ResponseEntity<ChatResponse<MessageDto>> chat(@RequestBody ChatRequest request) {
        MessageDto response = chatService.processChat(request);
        return ResponseEntity.ok(ChatResponse.success(response));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ChatResponse<List<ConversationDto>>> getConversations() {
        List<ConversationDto> conversations = chatService.getAllConversations();
        return ResponseEntity.ok(ChatResponse.success(conversations));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ChatResponse<List<ConversationDto>>> getMessages(@PathVariable Long id) {
        List<ConversationDto> messages = chatService.getMessagesByConversationId(id);
        return ResponseEntity.ok(ChatResponse.success(messages));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ChatResponse<Void>> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ResponseEntity.ok(ChatResponse.success(null));
    }


}

package com.rocket.chatbot.conversation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<MessageDto>> chat(@RequestBody ChatRequest request) {
        MessageDto response = chatService.processChat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getConversations() {
        List<ConversationDto> conversations = chatService.getAllConversations();
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(@PathVariable Long id) {
        List<MessageDto> messages = chatService.getMessagesByConversationId(id);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

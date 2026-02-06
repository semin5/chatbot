package com.rocket.chatbot.dto;

import com.rocket.chatbot.domain.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    private Long conversationId;
    private String message;
}

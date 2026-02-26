package com.rocket.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    @JsonIgnore
    private Long conversationId;
    private String message;
}

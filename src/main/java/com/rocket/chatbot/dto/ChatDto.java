package com.rocket.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatDto {
    private String model;
    private List<OpenAIMessage> messages;
    private boolean stream;
}

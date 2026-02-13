package com.rocket.chatbot.dto;

import com.rocket.chatbot.domain.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @Getter
    @Setter
    public static class Choice {
        private Message message;
    }
}

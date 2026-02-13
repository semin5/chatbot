package com.rocket.chatbot.dto;

import com.rocket.chatbot.domain.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
public class Chatbody {
    private String model;
    private List<Message> messages;
    private boolean stream;
}

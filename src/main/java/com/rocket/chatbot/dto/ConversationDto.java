package com.rocket.chatbot.dto;

import com.rocket.chatbot.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConversationDto {
    private String title;
    private User user;
}

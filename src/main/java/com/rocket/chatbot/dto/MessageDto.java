package com.rocket.chatbot.dto;

import com.rocket.chatbot.domain.Conversation;
import com.rocket.chatbot.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageDto {
    private Conversation conversation;
    private String role;
    private String content;

    public static MessageDto from(Message assistantMessage){
        return new MessageDto(
            assistantMessage.getConversation(),
            assistantMessage.getRole(),
            assistantMessage.getContent()
        );
    }
}

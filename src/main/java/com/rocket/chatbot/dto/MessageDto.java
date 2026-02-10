package com.rocket.chatbot.dto;

import com.rocket.chatbot.domain.Conversation;
import com.rocket.chatbot.domain.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
    private Long messageId;
    private String role;
    private String content;

    public MessageDto(Long id, String role, String content) {
        this.messageId = id;
        this.role = role;
        this.content = content;
    }

    public MessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static MessageDto detail(Message m){
        return new MessageDto(
                m.getId(),
                m.getRole(),
                m.getContent()
        );
    }

    public static MessageDto from(Message m){
        return new MessageDto(
            m.getRole(),
            m.getContent()
        );
    }
}

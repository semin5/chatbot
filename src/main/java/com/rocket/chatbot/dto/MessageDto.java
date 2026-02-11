package com.rocket.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rocket.chatbot.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long messageId;
    private String role;
    private String content;

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

package com.rocket.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rocket.chatbot.domain.Conversation;
import com.rocket.chatbot.domain.Message;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationDto {
    private Long conversationId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageDto> messages;

    public ConversationDto(Long id,
                           String title,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.conversationId = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ConversationDto(Long id,
                           String title,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           List<MessageDto> messages) {
        this.conversationId = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    public static ConversationDto from(Conversation c) {
        return new ConversationDto(
                c.getId(),
                c.getTitle(),
                c.getCreatedAt(),
                c.getUpdatedAt()
                );
    }

    public static ConversationDto detail(Conversation c, List<Message> messages) {
        return new ConversationDto(
                c.getId(),
                c.getTitle(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                messages.stream().map(MessageDto::detail).toList()
        );
    }
}

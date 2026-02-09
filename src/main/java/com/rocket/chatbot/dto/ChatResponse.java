package com.rocket.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse<T> {

    private int status;
    private String message;
    private T data;

    public static <T> ChatResponse<T> success(T data) {
        return ChatResponse.<T>builder()
                .status(200)
                .message("SUCCESS")
                .data(data)
                .build();
    }

    public static <T> ChatResponse<T> success(T data, String message) {
        return ChatResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ChatResponse<T> error(int status, String message) {
        return ChatResponse.<T>builder()
                .status(status)
                .message(message)
                .build();
    }
}

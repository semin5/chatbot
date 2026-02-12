package com.rocket.chatbot.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청"),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 인수"),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없슴"),

    // 429
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청 너무 많음"),

    // 502
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출 실패"),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류");

    private final HttpStatus status;
    private final String message;
}

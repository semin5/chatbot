package com.rocket.chatbot.exception;

import com.rocket.chatbot.config.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private HttpStatus safeStatus(int code) {
        HttpStatus status = HttpStatus.resolve(code);
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // 400 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e){

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(a ->
                        a.getField() + ": " + a.getDefaultMessage())
                .orElse("잘못된 요청");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        message,
                        "잘못된 요청"));
    }

    // 400 잘못된 인수
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException e){

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        HttpStatus.BAD_REQUEST,
                        e.getMessage(),
                        "데이터 없음"));
    }

    // 404 못찾음
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse> handleNotFound(NoSuchElementException e) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        HttpStatus.NOT_FOUND,
                        e.getMessage(),
                        "찾을 수 없음"));
    }

    // 429 상태 코드
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse> handleResponseStatus(ResponseStatusException e){

        HttpStatus status = safeStatus(e.getStatusCode().value());
        String message = (e.getReason() != null) ? e.getReason() : "요청 처리 중 오류";

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(
                        status,
                        message,
                        "요청 처리 중 오류"));
    }

    // 429 외부 API 오류
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse> handleWebClient(WebClientResponseException e){

        HttpStatus status = safeStatus(e.getStatusCode().value());

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(
                        status,
                        "외부 API 호출 실패",
                        "외부 API 오류"));
    }

    // 서버 내부 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage(),
                        "서버 내부 오류 발생"));
    }

    // 기타 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage(),
                        "예상치 못한 오류 발생"));
    }
}

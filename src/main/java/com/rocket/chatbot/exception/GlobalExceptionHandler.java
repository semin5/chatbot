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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusiness(BusinessException e){

        ErrorCode c = e.getErrorCode();

        return ResponseEntity
                .status(c.getStatus())
                .body(ApiResponse.error(
                        c.getStatus(),
                        e.getMessage(),
                        e.getData()));
    }

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

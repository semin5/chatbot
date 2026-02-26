package com.rocket.chatbot.controller;

import com.rocket.chatbot.config.ApiResponse;
import com.rocket.chatbot.dto.HealthDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Operation(summary = "헬스 체크")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthDto>> health() {

        return ResponseEntity.ok(ApiResponse.success(new HealthDto()));
    }
}

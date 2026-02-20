package com.rocket.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@NoArgsConstructor
public class HealthDto {
    private String status = "Running";
    private String time = OffsetDateTime.now(ZoneOffset.UTC).toString();
}
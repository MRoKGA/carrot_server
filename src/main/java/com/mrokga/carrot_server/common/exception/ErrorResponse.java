package com.mrokga.carrot_server.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private Instant timestamp;
    private String path;
    private String message;
    private String code;
    private Map<String, String> errors; // 필드 에러
}

package com.mrokga.carrot_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int code;

    @Schema(description = "결과 메시지", example = "success")
    private String message;

    @Schema(description = "결과 데이터. nullable")
    private T data;

    public static <T> ApiResponseDto<T> success(int code, String message, T data) {
        return new ApiResponseDto<>(code, message, data);
    }

    public static <T> ApiResponseDto<T> success(int code, String message) {
        return new ApiResponseDto<>(code, message, null);
    }

    public static <T> ApiResponseDto<T> error(int code, String message, T data) {
        return new ApiResponseDto<>(code, message, data);
    }

    public static <T> ApiResponseDto<T> error(int code, String message) {
        return new ApiResponseDto<>(code, message, null);
    }
}

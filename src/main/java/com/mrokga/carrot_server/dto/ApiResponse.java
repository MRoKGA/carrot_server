package com.mrokga.carrot_server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;       // HTTP 상태 코드 (예: 200, 201, 400)
    private String status;  // 상태 문자열 (예: "OK", "CREATED", "BAD_REQUEST")
    private T data;         // 가변 데이터

    public static <T> ApiResponse<T> success(int code, String status, T data) {
        return new ApiResponse<>(code, status, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "OK", data);
    }

    public static <T> ApiResponse<T> error(int code, String status, T data) {
        return new ApiResponse<>(code, status, data);
    }

    public static <T> ApiResponse<T> error(int code, String status) {
        return new ApiResponse<>(code, status, null);
    }
}

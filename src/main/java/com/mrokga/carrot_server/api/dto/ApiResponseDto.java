package com.mrokga.carrot_server.api.dto;

import com.mrokga.carrot_server.auth.dto.response.LoginResponseDto;
import com.mrokga.carrot_server.product.dto.response.ProductDetailResponseDto;
import com.mrokga.carrot_server.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

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

    @Schema(name = "ApiLoginResponse", description = "로그인 요청 응답 예시용 class")
    public static class ApiLoginResponse extends ApiResponseDto<LoginResponseDto> {}

    @Schema(name = "ApiProductResponse", description = "product 응답 예시용 class")
    public static class ApiProductResponse extends ApiResponseDto<Product> {}

    @Schema(name = "ApiProductListResponse", description = "상품 목록 응답 예시용 class")
    public static class ApiProductListResponse extends ApiResponseDto<List<ProductDetailResponseDto>> {}

    @Schema(name = "ApiPageProductResponse", description = "상품 검색 결과 응답 예시용 class")
    public static class ApiPageProductResponse extends ApiResponseDto<Page<ProductDetailResponseDto>> {}
}

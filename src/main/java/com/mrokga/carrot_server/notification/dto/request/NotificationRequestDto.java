package com.mrokga.carrot_server.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "푸시 알림 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    @NotNull
    @Schema(description = "유저 id", example = "7")
    private String userId;

    @NotNull
    @Schema(description = "제목", example = "상품 등록 알림")
    private String title;

    @NotNull
    @Schema(description = "내용", example = "관심 카테고리에 새로운 상품 등록 ")
    private String content;
}

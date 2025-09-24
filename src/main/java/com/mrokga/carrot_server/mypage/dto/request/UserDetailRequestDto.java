package com.mrokga.carrot_server.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UserDetailRequestDto", description = "userId로 사용자 + 동네 정보를 조회하기 위한 요청 본문")
public record UserDetailRequestDto(

        @Schema(description = "조회할 사용자 ID", example = "11", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "userId는 필수입니다.")
        @Min(value = 1, message = "userId는 입력.")
        Integer userId
) {}
package com.mrokga.carrot_server.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "인증번호 인증 요청 DTO")
@Getter
public class VerifyCodeRequestDto {
    @Schema(description = "사용자 휴대폰 번호", example = "01028369068")
    private String phoneNumber;

    @Schema(description = "인증번호", example = "000000")
    private String code;
}

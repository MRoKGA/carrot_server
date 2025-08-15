package com.mrokga.carrot_server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "인증번호 발송 요청 DTO")
@Getter
public class AuthSendRequestDto {
    @Schema(description = "사용자 휴대폰 번호", example = "01028369068")
    private String phoneNumber;
}

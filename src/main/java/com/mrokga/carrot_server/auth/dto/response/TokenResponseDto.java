package com.mrokga.carrot_server.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "토큰 응답 DTO")
@Getter
@Builder
public class TokenResponseDto {

    private String accessToken;

    private String refreshToken;

    private long expiresInSeconds;
}

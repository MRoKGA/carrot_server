package com.mrokga.carrot_server.Auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "회원가입 요청 DTO")
@Getter
@Setter
public class SignupRequestDto {

    @Schema(description = "동네 이름", example = "서울 동작구 대방동")
    private String region;

    @Schema(description = "휴대폰 번호", example = "01028369068")
    private String phoneNumber;

    @Schema(description = "닉네임", example = "닉네임")
    private String nickname;

    @Schema(hidden = true)
    private String profileImageUrl;
}

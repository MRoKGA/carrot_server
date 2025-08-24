package com.mrokga.carrot_server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "회원가입 요청 DTO")
@Getter
public class SignupRequestDto {

    @Schema(description = "동네 이름", example = "서울 동작구 대방동")
    private String region;

    @Schema(description = "휴대폰 번호", example = "01028369068")
    private String phoneNumber;

    @Schema(description = "닉네임", example = "닉네임")
    private String nickname;

    @Schema(description = "프로필 사진 url", example = "url")
    private String profileImageUrl;
}

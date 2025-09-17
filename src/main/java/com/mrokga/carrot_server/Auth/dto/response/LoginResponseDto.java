package com.mrokga.carrot_server.Auth.dto.response;

import com.mrokga.carrot_server.User.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Schema(description = "로그인 응답 DTO")
@Getter
@ToString
public class LoginResponseDto {

    @Schema(description = "토큰 정보")
    private TokenResponseDto token;

    @Schema(description = "유저 정보")
    private UserDto user;

    public LoginResponseDto(TokenResponseDto tokenResponseDto, UserDto userDto) {
        this.token = tokenResponseDto;
        this.user = userDto;
    }
}

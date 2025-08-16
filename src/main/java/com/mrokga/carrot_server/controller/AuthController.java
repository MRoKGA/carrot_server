package com.mrokga.carrot_server.controller;

import com.mrokga.carrot_server.dto.ApiResponseDto;
import com.mrokga.carrot_server.dto.request.AuthSendRequestDto;
import com.mrokga.carrot_server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authorization API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send")
    @Operation(summary = "인증번호 sms 발송", description = "사용자 휴대폰 번호로 인증번호 sms 발송")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 발송 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto> sendSms(@RequestBody AuthSendRequestDto req) {
        authService.sendSms(req.getPhoneNumber());

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success"));
    }
}

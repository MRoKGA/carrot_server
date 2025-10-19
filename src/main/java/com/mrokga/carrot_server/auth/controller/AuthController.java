package com.mrokga.carrot_server.auth.controller;

import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import com.mrokga.carrot_server.user.dto.UserDto;
import com.mrokga.carrot_server.auth.dto.request.SignupRequestDto;
import com.mrokga.carrot_server.auth.dto.request.VerifyCodeRequestDto;
import com.mrokga.carrot_server.auth.dto.response.LoginResponseDto;
import com.mrokga.carrot_server.auth.dto.response.TokenResponseDto;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.auth.enums.VerifyCodeResult;
import com.mrokga.carrot_server.auth.service.AuthService;
import com.mrokga.carrot_server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authorization API", description = "인증 관련 API")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/send")
    @Operation(summary = "인증번호 sms 발송", description = "사용자 휴대폰 번호로 인증번호 sms 발송")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 발송 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Void>> sendSms(@Parameter(description = "휴대폰 번호", example = "01028369068") @RequestParam String phoneNumber) {
        authService.sendSms(phoneNumber);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success"));
    }

    @PostMapping("/verify")
    @Operation(summary = "인증번호 인증", description = "사용자가 입력한 인증번호와 redis에 저장된 값 비교")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 200, "message": "인증 성공", "data": null }
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "인증번호 불일치", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 400, "message": "인증코드 불일치", "data": null }
                            """)
            })),
            @ApiResponse(responseCode = "410", description = "인증번호 만료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 410, "message": "인증코드 만료", "data": null }
                            """)
            }))
    })
    public ResponseEntity<ApiResponseDto<Void>> verifyCode(@RequestBody VerifyCodeRequestDto request) {

        VerifyCodeResult result = authService.verifyCode(request.getPhoneNumber(), request.getCode());

        return switch (result) {
            case OK       -> ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), result.getMessage()));
            case MISMATCH -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(HttpStatus.BAD_REQUEST.value(), result.getMessage()));
            case EXPIRED  -> ResponseEntity.status(HttpStatus.GONE).body(ApiResponseDto.error(HttpStatus.GONE.value(), result.getMessage()));
        };
    }

    @PostMapping("/validate-nickname")
    @Operation(summary = "닉네임 중복검사", description = "사용자가 입력한 닉네임이 중복되었는지 검사")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 x", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 200, "message": "사용 가능한 닉네임입니다.", "data": "닉네임" }
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "중복됨", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 400, "message": "중복된 닉네임입니다.", "data": "닉네임" }
                            """)
            }))
    })
    public ResponseEntity<ApiResponseDto<String>> validateNickname(@Parameter(description = "검사할 닉네임", example = "닉네임") @RequestParam String nickname) {
        boolean isDuplicated = userService.isDuplicateNickname(nickname);

        if (isDuplicated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(HttpStatus.BAD_REQUEST.value(), "중복된 닉네임입니다.", nickname));
        }

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "사용 가능한 닉네임입니다.", nickname));
    }


    @PostMapping("/signup")
    @Operation(summary = "회원가입 요청", description = "회원가입 요청")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 200, "message": "success", "data": "user 정보" }
                            """)
            }))
    })
    public ResponseEntity<ApiResponseDto<User>> signup(@RequestBody SignupRequestDto request) {
        User user = userService.signup(request);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", user));
    }

    @PostMapping("/resend")
    @Operation(summary = "인증번호 sms 재발송", description = "사용자 휴대폰 번호로 인증번호 sms 재발송")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 재발송 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<Void>> resendSms(@Parameter(description = "휴대폰 번호", example = "01028369068") @RequestParam String phoneNumber) {
        authService.resendSms(phoneNumber);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success"));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 요청", description = "로그인 요청")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.ApiLoginResponse.class), examples = {
                    @ExampleObject(value = """
                            {
                              "code": 200,
                              "message": "로그인 성공",
                              "data": {
                                "token": {
                                  "accessToken": "string",
                                  "refreshToken": "string",
                                  "expiresInSeconds": "long"
                                },
                                "user": {
                                  "id": "int",
                                  "nickname": "string",
                                  "email": "string"
                                }
                              }
                            }
                            """)
            })),
            @ApiResponse(responseCode = "400", description = "인증번호 불일치", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 400, "message": "인증코드 불일치", "data": null }
                            """)
            })),
            @ApiResponse(responseCode = "410", description = "인증번호 만료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class), examples = {
                    @ExampleObject(value = """
                            { "code": 410, "message": "인증코드 만료", "data": null }
                            """)
            }))
    })
    public ResponseEntity<ApiResponseDto> login(@RequestBody VerifyCodeRequestDto request) {
        VerifyCodeResult result = authService.verifyCode(request.getPhoneNumber(), request.getCode());

        return switch (result) {
            case OK -> {
                User user = userService.getUserByPhoneNumber(request.getPhoneNumber());
                TokenResponseDto tokenResponseDto = authService.issueAndReturnTokens(user);
                UserDto userDto = UserDto.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .build();

                LoginResponseDto loginResponseDto = new LoginResponseDto(tokenResponseDto, userDto);
                log.info("loginResponseDto = {}", loginResponseDto);

                yield ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "로그인 성공", loginResponseDto));
            }
            case MISMATCH -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(HttpStatus.BAD_REQUEST.value(), result.getMessage()));
            case EXPIRED -> ResponseEntity.status(HttpStatus.GONE).body(ApiResponseDto.error(HttpStatus.GONE.value(), result.getMessage()));
        };
    }
}

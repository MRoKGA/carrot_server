package com.mrokga.carrot_server.Auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.Aws.Service.AwsS3Service;
import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import com.mrokga.carrot_server.User.dto.UserDto;
import com.mrokga.carrot_server.Auth.dto.request.SignupRequestDto;
import com.mrokga.carrot_server.Auth.dto.request.VerifyCodeRequestDto;
import com.mrokga.carrot_server.Auth.dto.response.LoginResponseDto;
import com.mrokga.carrot_server.Auth.dto.response.TokenResponseDto;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.Auth.enums.VerifyCodeResult;
import com.mrokga.carrot_server.Auth.service.AuthService;
import com.mrokga.carrot_server.User.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authorization API", description = "인증 관련 API")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AwsS3Service awsS3Service;

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


    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<User>> signup(
            // 1. JSON 데이터를 String으로 받도록 변경
            @RequestPart("request") String requestJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws Exception { // 👈 throws Exception 추가

        // 2. ObjectMapper를 사용해 DTO로 직접 변환
        ObjectMapper objectMapper = new ObjectMapper();
        SignupRequestDto request = objectMapper.readValue(requestJson, SignupRequestDto.class);

        // --- 이하 로직은 기존과 동일 ---
        String profileImageUrl;

        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = awsS3Service.uploadOne(profileImage);
        } else {
            profileImageUrl = AuthService.DEFAULT_PROFILE_IMAGE;
        }

        request.setProfileImageUrl(profileImageUrl);

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

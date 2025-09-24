package com.mrokga.carrot_server.mypage.controller;

import com.mrokga.carrot_server.mypage.dto.request.UserDetailRequestDto;
import com.mrokga.carrot_server.mypage.dto.response.UserWithRegionsResponse;
import com.mrokga.carrot_server.mypage.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserQueryService userQueryService;

    @Operation(
            summary = "[GET] 사용자 + 동네 목록 조회",
            description = """
                    사용자 ID로 users 기본 정보와 user_region(대표/활성/인증시간) + region(행정동 상세)을 함께 반환합니다.
                    - PathVariable로 userId를 전달합니다.
                    - 성공 시 200과 함께 UserWithRegionsResponse를 반환합니다.
                    """,
            parameters = {
                    @Parameter(name = "userId", in = ParameterIn.PATH, required = true, description = "조회할 사용자 ID", example = "11")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserWithRegionsResponse.class),
                                    examples = @ExampleObject(name = "success",
                                            value = """
                                                    {
                                                      "user": {
                                                        "id": 11,
                                                        "email": "test@example.com",
                                                        "nickname": "동국",
                                                        "phoneNumber": "010-1234-5678",
                                                        "profileImageUrl": "https://.../avatar.png",
                                                        "mannerTemperature": 36.5,
                                                        "createdAt": "2025-09-01T12:34:56",
                                                        "updatedAt": null
                                                      },
                                                      "regions": [
                                                        {
                                                          "id": 101,
                                                          "isPrimary": true,
                                                          "isActive": true,
                                                          "verifiedAt": "2025-09-01T12:40:00",
                                                          "verifyExpiredAt": "2025-09-08T12:40:00",
                                                          "createdAt": "2025-09-01T12:35:00",
                                                          "updatedAt": null,
                                                          "region": {
                                                            "id": 417,
                                                            "name": "회기동",
                                                            "fullName": "서울특별시 동대문구 회기동",
                                                            "code": "11000-11230-...",
                                                            "centroid": "POINT(126.92637 37.512554)"
                                                          }
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ))),
                    @ApiResponse(responseCode = "404", description = "사용자 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "not_found",
                                            value = """
                                                    {
                                                      "code": "NOT_FOUND",
                                                      "message": "User not found: id=11"
                                                    }
                                                    """
                                    )))
            }
    )
    @GetMapping("/{userId}/full")
    public ResponseEntity<UserWithRegionsResponse> getUserWithRegions(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(userQueryService.getUserWithRegions(userId));
    }

    @Operation(
            summary = "[POST] 사용자 + 동네 목록 조회 (요청 본문)",
            description = """
                    사용자 ID로 users 기본 정보와 user_region(대표/활성/인증시간) + region(행정동 상세)을 함께 반환합니다.
                    - Request Body(UserDetailRequestDto)로 userId를 전달합니다.
                    - Swagger에서 Try it out 시 편리합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDetailRequestDto.class),
                            examples = @ExampleObject(name = "request",
                                    value = "{ \"userId\": 11 }"
                            ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = UserWithRegionsResponse.class))),
                    @ApiResponse(responseCode = "404", description = "사용자 없음")
            }
    )
    @PostMapping("/full")
    public ResponseEntity<UserWithRegionsResponse> postUserWithRegions(
            @Valid @RequestBody UserDetailRequestDto dto
    ) {
        return ResponseEntity.ok(userQueryService.getUserWithRegions(dto.userId()));
    }
}

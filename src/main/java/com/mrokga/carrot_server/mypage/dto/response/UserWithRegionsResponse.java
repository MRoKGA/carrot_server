package com.mrokga.carrot_server.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserWithRegionsResponse", description = "사용자 + 사용자 동네 목록 응답")
public record UserWithRegionsResponse(
        @Schema(description = "사용자 기본 정보") UserSummaryDto user,
        @ArraySchema(schema = @Schema(implementation = UserRegionDto.class), arraySchema = @Schema(description = "사용자 동네 목록"))
        List<UserRegionDto> regions
) {}
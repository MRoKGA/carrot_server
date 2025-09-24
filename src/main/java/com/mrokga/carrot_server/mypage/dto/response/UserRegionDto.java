package com.mrokga.carrot_server.mypage.dto.response;

import com.mrokga.carrot_server.Region.entity.UserRegion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "UserRegionDto", description = "사용자-동네 매핑 정보")
public record UserRegionDto(
        @Schema(description = "UserRegion 매핑 ID", example = "101") Integer id,
        @Schema(description = "대표 동네 여부", example = "true") Boolean isPrimary,
        @Schema(description = "활성 동네 여부", example = "true") Boolean isActive,
        @Schema(description = "동네 인증 완료 시각") LocalDateTime verifiedAt,
        @Schema(description = "동네 인증 만료 시각") LocalDateTime verifyExpiredAt,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt,
        @Schema(description = "행정동 정보") RegionSummaryDto region
) {
    public static UserRegionDto from(UserRegion ur) {
        return new UserRegionDto(
                ur.getId(),
                ur.getIsPrimary(),
                ur.getIsActive(),
                ur.getVerifiedAt(),
                ur.getVerifyExpiredAt(),
                ur.getCreatedAt(),
                ur.getUpdatedAt(),
                RegionSummaryDto.from(ur.getRegion())
        );
    }
}
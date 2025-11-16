package com.mrokga.carrot_server.mypage.dto.response;

import com.mrokga.carrot_server.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "UserSummaryDto", description = "사용자 기본 정보")
public record UserSummaryDto(
        @Schema(description = "사용자 ID", example = "11") Integer id,
        @Schema(description = "이메일(선택 값)", example = "test@example.com") String email,
        @Schema(description = "닉네임", example = "동국") String nickname,
        @Schema(description = "휴대폰 번호", example = "010-1234-5678") String phoneNumber,
        @Schema(description = "프로필 이미지 URL", example = "https://.../avatar.png") String profileImageUrl,
        @Schema(description = "매너온도", example = "36.5") Double mannerTemperature,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt
) {
    public static UserSummaryDto from(User u) {
        return new UserSummaryDto(
                u.getId(),
                u.getEmail(),
                u.getNickname(),
                u.getPhoneNumber(),
                u.getProfileImageUrl(),
                u.getMannerTemperature(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
package com.mrokga.carrot_server.group.dto;

import com.mrokga.carrot_server.group.entity.Group;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(name = "GroupCreateRequest", description = "동네모임 생성 요청")
public class GroupCreateRequest {

    @Schema(description = "모임명", example = "회기동 아침조깅 모임", maxLength = 40, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "소개/설명", example = "매주 토·일 오전 7시에 회기역 근처에서 함께 조깅해요!", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "지역 ID", example = "366", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer regionId;

    @Schema(description = "커버 이미지 URL", example = "https://cdn.example.com/groups/jogging.jpg")
    private String coverImageUrl;

    @Schema(description = "공개 범위(PUBLIC/PRIVATE)", example = "PUBLIC", requiredMode = Schema.RequiredMode.REQUIRED)
    private Group.Visibility visibility;

    @Schema(description = "가입 정책(OPEN/APPROVAL/INVITE_ONLY)", example = "APPROVAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private Group.JoinPolicy joinPolicy;

    @Schema(description = "최대 인원(없으면 null)", example = "50")
    private Integer maxMembers;
}

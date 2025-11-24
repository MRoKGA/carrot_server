package com.mrokga.carrot_server.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GroupResponse", description = "동네모임 응답")
public class GroupResponse {
    @Schema(description = "그룹 ID", example = "123")
    private Integer id;

    @Schema(description = "모임명", example = "회기동 아침조깅 모임")
    private String name;

    @Schema(description = "설명", example = "매주 토·일 오전 7시에 회기역 근처에서 함께 조깅해요!")
    private String description;

    @Schema(description = "지역 ID", example = "366")
    private Integer regionId;

    @Schema(description = "지역명", example = "회기동")
    private String regionName;

    @Schema(description = "커버 이미지", example = "https://cdn.example.com/groups/jogging.jpg")
    private String coverImageUrl;

    @Schema(description = "공개범위", example = "PUBLIC", allowableValues = {"PUBLIC","PRIVATE"})
    private String visibility;

    @Schema(description = "가입정책", example = "APPROVAL", allowableValues = {"OPEN","APPROVAL","INVITE_ONLY"})
    private String joinPolicy;

    @Schema(description = "현재 인원 수", example = "18")
    private Integer memberCount;

    @Schema(description = "최대 인원", example = "50")
    private Integer maxMembers;

    @Schema(description = "내가 가입된 모임인가", example = "true")
    private boolean member;

    @Schema(description = "내 역할", example = "OWNER", allowableValues = {"OWNER","MANAGER","MEMBER","NONE"})
    private String role;

    @Schema(description = "생성 시각", example = "2025-10-29T08:30:00")
    private LocalDateTime createdAt;
}

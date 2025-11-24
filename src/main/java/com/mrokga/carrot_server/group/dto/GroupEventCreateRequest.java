package com.mrokga.carrot_server.group.dto;

import com.mrokga.carrot_server.region.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(name = "GroupEventCreateRequest", description = "모임 이벤트 생성 요청")
public class GroupEventCreateRequest {

    @Schema(description = "제목", example = "11/2(일) 아침 조깅")
    private String title;

    @Schema(description = "설명", example = "회기역 2번출구 7:00 집결, 초보 환영!")
    private String description;

    @Schema(description = "장소 정보")
    private LocationDto location;

    @Schema(description = "시작 시각", example = "2025-11-02T07:00:00")
    private LocalDateTime startAt;

    @Schema(description = "종료 시각", example = "2025-11-02T08:30:00")
    private LocalDateTime endAt;

    @Schema(description = "정원(없으면 null)", example = "20")
    private Integer capacity;

    @Schema(description = "참가비(원, 없으면 null)", example = "0")
    private Integer fee;
}

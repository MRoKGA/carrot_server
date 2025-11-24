package com.mrokga.carrot_server.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mrokga.carrot_server.region.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "GroupEventResponse", description = "모임 이벤트 응답")
public class GroupEventResponse {
    @Schema(description = "이벤트 ID", example = "10")
    private Integer id;

    @Schema(description = "그룹 ID", example = "123")
    private Integer groupId;

    @Schema(description = "제목", example = "11/2(일) 아침 조깅")
    private String title;

    @Schema(description = "설명", example = "회기역 2번출구 7:00 집결, 초보 환영!")
    private String description;

    @Schema(description = "장소")
    private LocationDto location;

    @Schema(description = "시작", example = "2025-11-02T07:00:00")
    private LocalDateTime startAt;

    @Schema(description = "종료", example = "2025-11-02T08:30:00")
    private LocalDateTime endAt;

    @Schema(description = "정원", example = "20")
    private Integer capacity;

    @Schema(description = "참가비", example = "0")
    private Integer fee;

    @Schema(description = "참석(going) 수", example = "12")
    private long goingCount;

    @Schema(description = "관심(interested) 수", example = "7")
    private long interestedCount;

    @Schema(description = "나의 RSVP 상태", example = "GOING", allowableValues = {"GOING","INTERESTED","NONE","CANCELLED"})
    private String myRsvp;
}

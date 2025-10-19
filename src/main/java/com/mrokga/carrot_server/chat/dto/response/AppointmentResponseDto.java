package com.mrokga.carrot_server.chat.dto.response;

import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDto {
    @Schema(description = "약속 ID", example = "10")
    private Integer id;

    @Schema(description = "채팅방 ID", example = "3")
    private Integer chatRoomId;

    @Schema(description = "약속 제안자 ID", example = "5")
    private Integer proposerId;

    @Schema(description = "약속 시간", example = "2025-10-05T15:00:00")
    private LocalDateTime meetingTime;

    @Schema(description = "약속 장소", example = "강남역 2번 출구 앞")
    private String meetingPlace;

    @Schema(description = "약속 상태", example = "PENDING")
    private AppointmentStatus status;

    @Schema(description = "약속 생성 시각", example = "2025-09-30T14:22:00")
    private LocalDateTime createdAt;
}

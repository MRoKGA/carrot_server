package com.mrokga.carrot_server.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDto {
    @Schema(description = "약속 제안자 ID (구매자/판매자 상관없이 채팅방 참여자)", example = "5")
    @NotNull
    private Integer proposerId;

    @Schema(description = "만남 시간", example = "2025-10-05T15:00:00")
    @NotNull
    @Future
    private LocalDateTime meetingTime;

    @Schema(description = "만남 장소", example = "강남역 2번 출구 앞")
    private String meetingPlace;
}

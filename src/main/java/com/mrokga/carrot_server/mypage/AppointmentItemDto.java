// src/main/java/.../mypage/dto/AppointmentItemDto.java
package com.mrokga.carrot_server.mypage.dto;

import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "나의 약속(거래 예약) DTO")
public class AppointmentItemDto {
    private Integer appointmentId;
    private Integer roomId;

    private Integer productId;
    private String productTitle;

    private Integer proposerId;
    private String proposerNickname;

    private Integer counterpartId;       // 상대방
    private String counterpartNickname;  // 상대방 닉네임(내 기준)

    private LocalDateTime meetingTime;
    private String meetingPlace;

    private AppointmentStatus status;

    private LocalDateTime createdAt;
}

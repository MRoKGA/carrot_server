package com.mrokga.carrot_server.chat.dto.request;

import com.mrokga.carrot_server.chat.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "메세지 전송 요청 DTO")
public class MessageRequestDto {
    @NotNull
    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @NotBlank
    @Schema(description = "메세지 내용", example = "2025년 8월 26일 센트럴시티 스타벅스임")
    private String message;

    @Schema(description = "메세지 타입", example = "TEXT")
    private MessageType messageType;

    @Schema(description = "부모 메세지 ID(엔티티 셀프 조인)", example = "1")
    private Integer replyToMessageId;
}

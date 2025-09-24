package com.mrokga.carrot_server.Chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "전송된 메세지 응답 DTO")
public class MessageResponseDto {

    @Schema(description = "메세지 ID", example = "1")
    private Integer id;

    @Schema(description = "전송자 ID", example = "1")
    private Integer senderId;

    @Schema(description = "채팅방 ID", example = "1")
    private Integer chatRoomId;

    @Schema(description = "메시지 타입(TEXT/IMAGE)", example = "TEXT")
    private String messageType;

    @Schema(description = "메세지 내용", example = "8월 26일 화요일 오후 4시 23분 일까?")
    private String message;

    @Schema(description = "전송 시간", example = "2025-08-25T16:21:10")
    private LocalDateTime createdAt;

    // ===== 답장 요약 정보(부모가 있을 때만 채워짐) =====
    @Schema(
            description = "이 메시지가 ‘답장’인 경우, 대상 원본문의 ID. 일반 메시지는 null",
            example = "42",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer replyToMessageId;

    @Schema(
            description = "답장 대상 원본문의 작성자 ID. 일반 메시지는 null",
            example = "7",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer replyToSenderId;

    @Schema(
            description = "답장 대상 원본문의 미리보기 텍스트. TEXT는 앞 50자, IMAGE는 \"[이미지]\" 등. 일반 메시지는 null",
            example = "네, 가능합니다! 내일 오후 2시에...",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String replyToPreview;


    // 내 메세지 구분 및 읽음 표시 여부
    @Schema(description = "요청자(나)가 보낸 메시지인지 여부", example = "true")
    private boolean mine;

    @Schema(
            description = "요청자 기준으로, 이 메시지가 상대에게 읽혔는지 여부 "
                    + "(보통 '내 마지막 메시지'에만 true를 세팅하고 나머지는 false)",
            example = "true"
    )
    private boolean readByOpponent;
}

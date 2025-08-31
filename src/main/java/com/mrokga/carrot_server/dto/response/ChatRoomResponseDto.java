package com.mrokga.carrot_server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "채팅방 응답 DTO")
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID", example = "1")
    private Integer roomId;

    @Schema(description = "상품 ID", example = "1")
    private Integer productId;

    @Schema(description = "판매자 ID", example = "1")
    private Integer sellerId;

    @Schema(description = "구매자 ID", example = "1")
    private Integer buyerId;

    @Schema(description = "채팅방 생성 시간", example = "2025-08-25T16:21:10")
    private LocalDateTime createdAt;

    // --- 추가: 목록 미리보기 & 읽음 플래그 ---
    @Schema(
            description = "방의 마지막 메시지 ID(미리보기 식별용)",
            example = "12345",
            nullable = true
    )
    private Integer lastMessageId;

    @Schema(
            description = "방의 마지막 메시지를 보낸 사용자 ID",
            example = "21",
            nullable = true
    )
    private Integer lastMessageSenderId;

    @Schema(
            description = "마지막 메시지 미리보기 텍스트 (TEXT는 앞 50자, IMAGE는 \"[이미지]\")",
            example = "내일 오후 2시에 역 앞에서 뵐게요…",
            nullable = true
    )
    private String lastMessagePreview;

    @Schema(
            description = "마지막 메시지 작성 시각",
            example = "2025-08-25T16:22:33",
            nullable = true
    )
    private LocalDateTime lastMessageAt;

    @Schema(
            description = "인스타 DM 스타일: \"내 마지막 메시지\"가 상대에게 읽혔는가",
            example = "true"
    )
    private boolean lastMessageSeen;
}

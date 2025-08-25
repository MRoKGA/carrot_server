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
}

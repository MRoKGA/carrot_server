package com.mrokga.carrot_server.Chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NonNull;

@Data
@Schema(description = "채팅방 생성 및 불러오기 요청 DTO")
public class ChatRoomRequestDto {
    @NonNull
    @Schema(description = "상품 ID", example = "1")
    private Integer productId;

    @Schema(description = "판매자 ID", example = "1")
    private Integer sellerId;

    @Schema(description = "구매자 ID", example = "1")
    private Integer buyerId;
}

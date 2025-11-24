package com.mrokga.carrot_server.product.dto.response;

import com.mrokga.carrot_server.product.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "상품 거래 상태 변경 응답 DTO")
@Getter
@Builder
@AllArgsConstructor
public class ChangeStatusResponseDto {

    private Integer productId;

    private Integer sellerId;

    private TradeStatus status;

    private Integer buyerId; // nullable

    private LocalDateTime completedAt; // nullable
}

package com.mrokga.carrot_server.Product.dto.request;

import com.mrokga.carrot_server.Product.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "상품 거래 상태 변경 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeStatusRequestDto {

    @NotNull
    @Schema(description = "상품 id", example = "7")
    private Integer productId;

    @NotNull
    @Schema(description = "판매자 id", example = "7")
    private Integer sellerId;

    @NotNull
    @Schema(description = "거래 상태(ON_SALE, RESERVED, SOLD)", example = "RESERVED")
    private TradeStatus status;

    @Schema(description = "구매자 id", example = "")
    private Integer buyerId; // nullable

    @Schema(description = "거래 완료 시각", example = "")
    private LocalDateTime completedAt; // nullable
}

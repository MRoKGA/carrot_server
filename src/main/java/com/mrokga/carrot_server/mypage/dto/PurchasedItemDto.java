// src/main/java/.../mypage/dto/PurchasedItemDto.java
package com.mrokga.carrot_server.mypage.dto;

import com.mrokga.carrot_server.product.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "내가 구매한 물품 DTO")
public class PurchasedItemDto {
    private Integer transactionId;
    private Integer productId;

    private String title;
    private Integer price;
    private Boolean isFree;
    private TradeStatus status;

    private Integer sellerId;
    private String sellerNickname;

    private LocalDateTime createdAt;  // 상품 생성일
    private LocalDateTime completedAt; // 거래완료 시각(있으면)

    private String imageUrl;          // 썸네일
}

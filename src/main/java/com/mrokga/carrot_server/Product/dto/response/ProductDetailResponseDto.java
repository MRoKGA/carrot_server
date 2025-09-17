package com.mrokga.carrot_server.Product.dto.response;

import com.mrokga.carrot_server.Product.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "상품 상세 정보 응답 DTO")
@Getter
@Builder
@AllArgsConstructor
public class ProductDetailResponseDto {

    private String title;
    private String description;
    private String userName;
    private String regionName;
    private String categoryName;
    private int price;
    private boolean isFree;
    private TradeStatus status;
    private int favoriteCount;
    private int chatCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
}

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
    private Integer id;
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

    public ProductDetailResponseDto(
            Integer id,
            String title,
            String description,
            String userNickname,
            String categoryName,
            Integer price,
            Boolean isFree,
            TradeStatus status,
            int favoriteCount,
            int chatCount,
            String regionName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String imageUrl
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.userName = userNickname;
        this.categoryName = categoryName;
        this.price = price;
        this.isFree = isFree;
        this.status = status;
        this.favoriteCount = favoriteCount;
        this.chatCount = chatCount;
        this.regionName = regionName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.imageUrl = imageUrl;
    }
}


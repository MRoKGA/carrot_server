// src/main/java/.../product/dto/response/ProductListItemDto.java
package com.mrokga.carrot_server.product.dto.response;

import com.mrokga.carrot_server.product.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "상품 목록 아이템 DTO")
public class ProductListItemDto {

    private Integer id;
    private String title;
    private String description;

    private String userName;
    private String regionName;
    private String categoryName;

    private Integer price;
    private Boolean isFree;
    private TradeStatus status;

    private int favoriteCount;
    private int chatCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String imageUrl;       // 첫 번째(썸네일) 이미지
}

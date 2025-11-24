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
@Schema(description = "내 상품/찜/구매 공통 리스트 DTO")
public class MyProductDto {
    private Integer id;
    private String title;
    private String description;

    private String userName;     // 판매자 닉네임
    private String regionName;
    private String categoryName;

    private Integer price;
    private Boolean isFree;
    private TradeStatus status;

    private int favoriteCount;
    private int chatCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String imageUrl;     // 썸네일
}

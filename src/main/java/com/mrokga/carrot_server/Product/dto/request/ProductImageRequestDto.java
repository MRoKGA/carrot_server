package com.mrokga.carrot_server.Product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "상품 이미지 요청 DTO")
@Getter @Setter
public class ProductImageRequestDto {
    private String imageUrl;
    private int sortOrder;      // 0,1,2...
    private Boolean isThumbnail; // 선택: 없으면 서버에서 자동 지정
}

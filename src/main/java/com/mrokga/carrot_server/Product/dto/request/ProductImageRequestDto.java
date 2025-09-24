package com.mrokga.carrot_server.Product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "상품 이미지 요청 DTO")
@Getter
public class ProductImageRequestDto {

    private String imageUrl;
    private int sortOrder;
}

package com.mrokga.carrot_server.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(name = "ReviewCreateRequest")
public class ReviewCreateRequest {
    @Schema(description="구매 거래 ID", example="123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer transactionId;

    @Schema(description="상품 ID", example="15", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productId;

    @Schema(description="별점(1~5)", example="5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Schema(description="후기 내용(선택)", example="친절하고 약속도 잘 지키셨어요!")
    private String content;
}
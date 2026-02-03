package com.mrokga.carrot_server.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(name = "ReviewResponse")
public class ReviewResponse {
    private Integer id;
    private Integer transactionId;
    private Integer productId;
    private Integer buyerId;
    private String  buyerNickname;
    private Integer sellerId;
    private String  sellerNickname;
    private Integer rating;
    private String  content;
    private LocalDateTime createdAt;
}
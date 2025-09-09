package com.mrokga.carrot_server.dto.response;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mrokga.carrot_server.entity.Category;
import com.mrokga.carrot_server.entity.ProductImage;
import com.mrokga.carrot_server.entity.Region;
import com.mrokga.carrot_server.entity.User;
import com.mrokga.carrot_server.entity.embeddable.Location;
import com.mrokga.carrot_server.enums.TradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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

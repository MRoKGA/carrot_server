package com.mrokga.carrot_server.Product.dto.request;

import com.mrokga.carrot_server.Region.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter
@Schema(description = "상품 등록 요청 DTO")
public class CreateProductRequestDto {
    @Schema(example = "7")   private Integer userId;
    @Schema(example = "1")   private Integer categoryId;
    @Schema(example = "417") private Integer regionId;

    @Schema(example = "상품 제목 test")     private String title;
    @Schema(example = "상품 설명 test")     private String description;

    @Schema(example = "false") private Boolean isFree;
    @Schema(example = "10000") private Integer price;
    @Schema(example = "false") private Boolean isPriceSuggestable;

    private PreferredLocationDto preferredLocation;      // 선택
    private List<String> exposureRegions;                // 선택

    // 2-Step(이미지 URL로 등록) & 멀티파트(서버에서 URL 주입) 모두 호환
    private List<ProductImageRequestDto> images;

    @Getter @Setter
    public static class PreferredLocationDto {
        private Double latitude;
        private Double longitude;
        private String name;
    }
}
package com.mrokga.carrot_server.Chat.dto.request;

import com.mrokga.carrot_server.Region.dto.LocationDto;
import com.mrokga.carrot_server.Product.dto.request.ProductImageRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Schema(description = "상품 등록 요청 DTO")
@Getter
@ToString
public class CreateProductRequestDto {

    @NotNull
    @Schema(description = "판매자 id", example = "7")
    private Integer userId;

    @NotNull
    @Schema(description = "카테고리 id", example = "1")
    private Integer categoryId;

    @NotNull
    @Schema(description = "지역 id", example = "417")
    private Integer regionId;

    @NotBlank
    @Schema(description = "상품 제목", example = "상품 제목 test")
    private String title;

    @NotBlank
    @Schema(description = "상품 설명", example = "상품 설명 test")
    private String description;

    @NotNull
    @Min(0)
    @Schema(description = "가격", example = "10000")
    private Integer price;

    @NotNull
    @Schema(description = "나눔 여부", example = "false")
    private Boolean isFree;

    @NotNull
    @Schema(description = "가격 제안 가능 여부", example = "false")
    private Boolean isPriceSuggestable;

    @Schema(description = "상품 사진",
            example = "[{\"imageUrl\":\"https://example.com/image1.png\",\"sortOrder\":0}," +
                    "{\"imageUrl\":\"https://example.com/image2.png\",\"sortOrder\":1}]")
    private List<ProductImageRequestDto> images;

    @Schema(description = "거래 선호 지역",
            example = "{\"latitude\":37.5125541, \"longitude\":126.9263706, \"name\":\"대방역\"}")
    private LocationDto preferredLocation;

    @Schema(description = "상품 노출시킬 동네 리스트", example = "[\"회기동\"]")
    private List<String> exposureRegions;
}
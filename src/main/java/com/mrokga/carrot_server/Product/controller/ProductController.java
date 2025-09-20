package com.mrokga.carrot_server.Product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.Aws.Service.AwsS3Service;
import com.mrokga.carrot_server.Product.dto.request.ProductImageRequestDto;
import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import com.mrokga.carrot_server.Product.dto.request.ChangeStatusRequestDto;
import com.mrokga.carrot_server.Product.dto.request.CreateProductRequestDto;
import com.mrokga.carrot_server.Product.entity.Product;
import com.mrokga.carrot_server.Product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Tag(name = "Product API", description = "상품 관련 API")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final AwsS3Service awsS3Service;

    // 기존 JSON 방식 유지 (@RequestBody) — 2-Step 등록용
    @PostMapping
    @Operation(summary = "상품 등록(JSON, 이미지 URL 포함)", description = "이미지를 먼저 /file/upload로 올리고, 반환된 S3 URL을 본 API에 images.imageUrl로 전달하세요.")
    @ApiResponse(responseCode = "200", description = "상품 등록 성공", content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody CreateProductRequestDto req) {
        Product product = productService.createProduct(req);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", product));
    }

    // 신규: 멀티파트 한방 등록(JSON + 파일)
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 등록(멀티파트: JSON + 이미지 파일)",
            description = "{\n" +
                    "  \"userId\": 11,\n" +
                    "  \"categoryId\": 1,\n" +
                    "  \"regionId\": 366,\n" +
                    "  \"title\": \"로지텍마우스\",\n" +
                    "  \"description\": \"로지텍마우스\",\n" +
                    "  \"isFree\": false,\n" +
                    "  \"price\": 10000,\n" +
                    "  \"isPriceSuggestable\": false,\n" +
                    "  \"preferredLocation\": {\n" +
                    "    \"latitude\": 37.5125541,\n" +
                    "    \"longitude\": 126.9263706,\n" +
                    "    \"name\": \"회기동\"\n" +
                    "  },\n" +
                    "  \"exposureRegions\": [\"회기동\"]\n" +
                    "}")
    public ResponseEntity<ApiResponseDto<?>> createMultipart(
            @RequestPart("meta") String metaJson,              // ← String으로 받기
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {

        // JSON 문자열 → DTO
        CreateProductRequestDto meta = new ObjectMapper().readValue(metaJson, CreateProductRequestDto.class);

        if (images != null && !images.isEmpty()) {
            List<String> urls = awsS3Service.uploadFile(images);
            AtomicInteger idx = new AtomicInteger(0);
            var imageDtos = urls.stream().map(u -> {
                var dto = new ProductImageRequestDto();
                dto.setImageUrl(u);
                dto.setSortOrder(idx.getAndIncrement());
                return dto;
            }).toList();
            meta.setImages(imageDtos);
        }

        Product product = productService.createProduct(meta);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", product));
    }

    @Operation(summary = "상품 거래 상태 변경", description = "상품 거래 상태 변경")
    @PutMapping("/status")
    public ResponseEntity<?> changeStatus(@RequestBody ChangeStatusRequestDto req) {

        productService.changeStatus(req);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @Operation(summary = "상품 목록 조회", description = "현재 위치에 노출 설정한 상품 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<?>> getProductList(@Parameter(description = "유저 ID", example = "7") @RequestParam int userId) {
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", productService.getProductList(userId)));
    }

    @Operation(summary = "상품 상세 조회", description = "상품 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> getProductDetail(@Parameter(description = "상품 ID", example = "7") @PathVariable int id) {

        Product product = productService.getProductDetail(id);

        if (product != null) {
            return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", product));
        }

        return ResponseEntity.ok(ApiResponseDto.error(HttpStatus.NOT_FOUND.value(), "not found"));
    }

    @Operation(summary = "찜하기", description = "찜하기")
    @PostMapping("/{productId}/favorite")
    public ResponseEntity<?> favoriteProduct(@Parameter(description = "유저 ID", example = "7") @RequestParam int userId, @Parameter(description = "상품 ID", example = "7") @PathVariable int productId) {
        productService.toggleFavorite(userId, productId);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }
}

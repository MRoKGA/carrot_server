package com.mrokga.carrot_server.product.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.aws.service.AwsS3Service;
import com.mrokga.carrot_server.product.dto.request.ProductImageRequestDto;
import com.mrokga.carrot_server.product.dto.response.ProductDetailResponseDto;
import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import com.mrokga.carrot_server.product.dto.request.ChangeStatusRequestDto;
import com.mrokga.carrot_server.product.dto.request.CreateProductRequestDto;
import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    @Operation(
            summary = "상품 등록(멀티파트: JSON + 이미지 파일)",
            description = """
        폼 파트:
        - meta: application/json (CreateProductRequestDto)
        - images: file[] (여러 장 가능)
        업로드된 이미지는 S3 URL로 변환되어 meta.images에 주입됩니다.
        """
    )
    public ResponseEntity<ApiResponseDto<?>> createMultipart(
            @RequestPart("meta") String metaJson,                                // ← JSON 문자열로 받고
            @RequestPart(value = "images", required = false) List<MultipartFile> images // ← 여러 장 파일
    ) throws Exception {

        // 1) JSON 문자열 → DTO
        CreateProductRequestDto meta = new ObjectMapper().readValue(metaJson, CreateProductRequestDto.class);

        // 2) 파일이 있으면 S3 업로드 + 이미지 DTO 주입
        if (images != null && !images.isEmpty()) {

            // (선택) 간단한 이미지 MIME 검증
            for (MultipartFile f : images) {
                String ct = f.getContentType();
                if (ct == null || !ct.startsWith("image/")) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다: " + f.getOriginalFilename());
                }
            }

            // S3 업로드 (여러 장)
            List<String> urls = awsS3Service.uploadFile(images);

            // 업로드 순서대로 sortOrder 지정, 첫 번째 이미지를 썸네일로
            AtomicInteger idx = new AtomicInteger(0);
            List<ProductImageRequestDto> imageDtos = urls.stream()
                    .map(url -> {
                        ProductImageRequestDto dto = new ProductImageRequestDto();
                        dto.setImageUrl(url);
                        dto.setSortOrder(idx.get());                 // 0,1,2...
                        dto.setIsThumbnail(idx.get() == 0);          // 첫 번째만 썸네일
                        idx.incrementAndGet();
                        return dto;
                    })
                    .toList();

            meta.setImages(imageDtos);
        } else {
            meta.setImages(null); // 이미지 없이도 등록 가능하게
        }

        // 3) 서비스로 위임(여러 장 저장 + 정렬/썸네일 처리)
        Product product = productService.createProduct(meta);

        // 4) 응답
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
    public ResponseEntity<ApiResponseDto<?>> getProductList(@RequestParam int userId) {
        var list = productService.getProductList(userId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", list));
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
        productService.toggleFavorite(userId, "P", productId);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @Operation(summary = "상품 이름 검색", description = "사용자가 입력한 상품 이름으로 상품 목록 검색")
    @GetMapping("/search/{keyword}")
    public ResponseEntity<?> searchProduct(@PathVariable String keyword,
                                           @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<ProductDetailResponseDto> results = productService.searchProduct(keyword, pageable);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", results));
    }

    @Operation(
            summary = "상품 삭제",
            description = "상품 등록자(판매자)만 삭제할 수 있습니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> deleteProduct(
            @PathVariable int id,
            @Parameter(description = "판매자(요청자) ID", example = "11")
            @RequestParam int sellerId
    ) {
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "success", null)
        );
    }


}

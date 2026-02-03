package com.mrokga.carrot_server.product.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.aws.service.AwsS3Service;
import com.mrokga.carrot_server.product.dto.request.ProductImageRequestDto;
import com.mrokga.carrot_server.product.dto.response.ChangeStatusResponseDto;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * 상품 등록 api (이미지 URL을 JSON 내부에 포함하는 2-step 방식)
     * 이미지는 미리 S3에 업로드되어 URL 형태로 요청 본문에 포함되어야 함
     * @param req 상품 생성 요청 DTO (이미지 URL 포함)
     * @return 등록된 상품 정보
     */
    @PostMapping
    @Operation(summary = "상품 등록(JSON, 이미지 URL 포함)", description = "이미지를 먼저 /file/upload로 올리고, 반환된 S3 URL을 본 API에 images.imageUrl로 전달하세요.")
    @ApiResponse(responseCode = "200", description = "상품 등록 성공", content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody CreateProductRequestDto req) {
        Product product = productService.createProduct(req);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", product));
    }

    /**
     * 상품 정보와 이미지 파일을 한 번에 받아 처리하는 api
     * 이미지 파일을 S3에 업로드 후, 반환된 URL을 DTO에 주입하여 상품 등록 서비스에 전달
     * @param metaJson 상품 메타데이터 JSON 문자열 (CreateProductRequestDto)
     * @param images 업로드할 이미지 파일 리스트
     * @return 등록된 상품 정보
     * @throws Exception JSON 파싱 오류 또는 파일 처리 오류
     */
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

    /**
     * 상품의 거래 상태(TradeStatus)를 변경하는 api
     * @param req 상태 변경 요청 DTO
     * @return 변경된 거래 상태 정보가 담긴 응답 DTO
     */
    @Operation(summary = "상품 거래 상태 변경", description = "상품 거래 상태 변경")
    @PutMapping("/status")
    public ResponseEntity<?> changeStatus(@RequestBody ChangeStatusRequestDto req) {

        productService.changeStatus(req);

        // 상품 상태 변경 로직을 호출하고 결과를 응답 DTO에 저장
        ChangeStatusResponseDto response = productService.changeStatus(req);

        // 상태 변경 후 상품 상세 정보를 리턴
        Product product = productService.getProductDetail(req.getProductId());

        if (product != null) {

            return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", response));
        }

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    /**
     * 사용자의 현재 위치를 기준으로 상품 목록을 조회하는 api
     * @param userId 사용자의 ID
     * @return 페이지네이션된 상품 목록 DTO
     */
    @Operation(summary = "상품 목록 조회", description = "현재 위치에 노출 설정한 상품 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<?>> getProductList(@RequestParam int userId) {
        var list = productService.getProductList(userId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", list));
    }

    /**
     * 상품의 상세 정보를 조회하는 api
     * @param id 조회할 상품의 ID
     * @return 상품 상세 정보 DTO
     */
    @Operation(summary = "상품 상세 조회", description = "상품 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<?>> getProductDetail(@Parameter(description = "상품 ID", example = "7") @PathVariable int id) {

        Product product = productService.getProductDetail(id);

        if (product != null) {
            return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", product));
        }

        return ResponseEntity.ok(ApiResponseDto.error(HttpStatus.NOT_FOUND.value(), "not found"));
    }

    /**
     * 특정 상품을 찜하는 api. 토글 방식
     * @param userId 사용자 ID
     * @param productId 찜하기 대상 상품 ID
     * @return 성공 응답 (상태 변경 완료)
     */
    @Operation(summary = "찜하기", description = "찜하기")
    @PostMapping("/{productId}/favorite")
    public ResponseEntity<?> favoriteProduct(@Parameter(description = "유저 ID", example = "7") @RequestParam int userId, @Parameter(description = "상품 ID", example = "7") @PathVariable int productId) {
        productService.toggleFavorite(userId, "P", productId);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    /**
     * 상품 이름 키워드를 기반으로 상품 목록을 검색하고 페이지네이션하여 반환하는 api
     * @param keyword 검색할 상품 이름 키워드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 한 페이지당 상품 개수
     * @param sort 정렬 기준 필드
     * @return 페이지네이션된 검색 결과 DTO
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 검색 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.ApiPageProductResponse.class)))
    })
    public ResponseEntity<?> searchProduct(@Parameter(description = "검색할 키워드", example = "test") @PathVariable String keyword,
                                           @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
                                           @Parameter(description = "한 페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
                                           @Parameter(description = "정렬 기준 (예: createdAt)", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sort) {
        // Pageable 객체 생성: 페이지 번호, 크기, 정렬 기준으로 구성
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
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

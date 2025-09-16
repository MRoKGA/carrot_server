package com.mrokga.carrot_server.controller;

import com.mrokga.carrot_server.dto.ApiResponseDto;
import com.mrokga.carrot_server.dto.request.ChangeStatusRequestDto;
import com.mrokga.carrot_server.dto.request.CreateProductRequestDto;
import com.mrokga.carrot_server.entity.Product;
import com.mrokga.carrot_server.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Tag(name = "Product API", description = "상품 관련 API")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "상품 등록", description = "상품 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 등록 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class)))
    })
    public ResponseEntity<ApiResponseDto<?>> create(@RequestBody CreateProductRequestDto req) {

        Product product = productService.createProduct(req);

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

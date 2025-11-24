// src/main/java/.../mypage/controller/MyPageController.java
package com.mrokga.carrot_server.mypage.controller;

import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import com.mrokga.carrot_server.mypage.dto.AppointmentItemDto;
import com.mrokga.carrot_server.mypage.dto.MyProductDto;
import com.mrokga.carrot_server.mypage.dto.PurchasedItemDto;
import com.mrokga.carrot_server.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "MyPage API", description = "내 판매/구매/찜/약속 조회 API")
public class MyPageController {

    private final MyPageService myPageService;

    private Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        return Integer.valueOf(auth.getName());
    }

    @Operation(summary = "내가 판매하는 물품", description = "판매자=나 인 상품 목록")
    @GetMapping("/selling")
    public ResponseEntity<ApiResponseDto<?>> mySelling(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = myPageService.getMySellingProducts(getCurrentUserId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", result));
    }

    @Operation(summary = "내가 구매한 물품", description = "거래내역 중 buyer=나 인 트랜잭션 기반")
    @GetMapping("/purchases")
    public ResponseEntity<ApiResponseDto<?>> myPurchases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var result = myPageService.getMyPurchasedProducts(getCurrentUserId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", result));
    }

    @Operation(summary = "내가 찜한 물품", description = "Favorite(Product) 기준, 최신 즐겨찾기 순")
    @GetMapping("/favorites/products")
    public ResponseEntity<ApiResponseDto<?>> myFavoriteProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var result = myPageService.getMyFavoriteProducts(getCurrentUserId(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", result));
    }

    @Operation(summary = "나의 약속(거래 예약)", description = "채팅방 참여자 또는 제안자로서의 약속. status 필터 선택 가능(PENDING/ACCEPTED/REJECTED/CANCELED)")
    @GetMapping("/appointments")
    public ResponseEntity<ApiResponseDto<?>> myAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var result = myPageService.getMyAppointments(getCurrentUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", result));
    }
}

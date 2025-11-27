// package com.mrokga.carrot_server.product.controller;
package com.mrokga.carrot_server.product.controller;

import com.mrokga.carrot_server.product.dto.request.ReviewCreateRequest;
import com.mrokga.carrot_server.product.dto.response.ReviewResponse;
import com.mrokga.carrot_server.product.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "후기(Review)")
public class ReviewController {

    private final ReviewService reviewService;

    private Integer me() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal()))
            throw new RuntimeException("UNAUTHORIZED");
        return Integer.valueOf(a.getName());
    }

    /** 후기 작성(구매자) */
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> create(@RequestBody ReviewCreateRequest req) {
        return ResponseEntity.ok(reviewService.createReview(me(), req));
    }

    /** 내가 받은 후기(판매자 입장) */
    @GetMapping("/users/{userId}/reviews/received")
    public Page<ReviewResponse> received(@PathVariable Integer userId,
                                         @ParameterObject @PageableDefault(size=20, sort = "id", direction=Sort.Direction.DESC) Pageable pg) {
        return reviewService.listReceived(userId, pg);
    }

    /** 내가 쓴 후기(구매자 입장) */
    @GetMapping("/users/{userId}/reviews/written")
    public Page<ReviewResponse> written(@PathVariable Integer userId,
                                        @ParameterObject @PageableDefault(size=20, sort = "id", direction=Sort.Direction.DESC) Pageable pg) {
        return reviewService.listWritten(userId, pg);
    }

    /** 상품별 후기(상세 페이지에서 사용 가능) */
    @GetMapping("/product/{productId}/reviews")
    public Page<ReviewResponse> byProduct(@PathVariable Integer productId,
                                          @ParameterObject @PageableDefault(size=20, sort = "id", direction=Sort.Direction.DESC) Pageable pg) {
        return reviewService.listByProduct(productId, pg);
    }
}

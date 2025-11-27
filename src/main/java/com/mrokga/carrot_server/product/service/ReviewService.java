// package com.mrokga.carrot_server.product.service;
package com.mrokga.carrot_server.product.service;

import com.mrokga.carrot_server.product.dto.request.ReviewCreateRequest;
import com.mrokga.carrot_server.product.dto.response.ReviewResponse;
import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.entity.Review;
import com.mrokga.carrot_server.product.enums.TradeStatus;
import com.mrokga.carrot_server.product.repository.ProductRepository;
import com.mrokga.carrot_server.product.repository.ReviewRepository;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    // private final ProductTransactionRepository txRepository; // 실제 사용 중인 거래 리포지토리로 교체

    private double deltaByRating(int rating) {
        return switch (rating) {
            case 1 -> -2.0;
            case 2 -> -1.0;
            case 3 ->  0.0;
            case 4 -> +1.0;
            case 5 -> +2.0;
            default -> throw new IllegalArgumentException("rating must be 1~5");
        };
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    @Transactional
    public ReviewResponse createReview(int meId, ReviewCreateRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5)
            throw new IllegalArgumentException("별점은 1~5입니다.");
        if (reviewRepository.existsByTransactionId(req.getTransactionId()))
            throw new IllegalStateException("이미 해당 거래에 대한 후기가 존재합니다.");

        // 1) 거래/상품 확인 (실제 구현에 맞게 바꾸세요)
        Product product = productRepository.findById(req.getProductId()).orElseThrow();
        // ProductTransaction tx = txRepository.findById(req.getTransactionId()).orElseThrow();

        // 가정: meId == tx.getBuyer().getId()
        // if (!tx.getBuyer().getId().equals(meId)) throw new IllegalStateException("구매자만 작성할 수 있습니다.");
        // if (tx.getStatus() != TradeStatus.SOLD) throw new IllegalStateException("거래완료 후 작성할 수 있습니다.");

        // 거래 테이블이 없다면: 상품이 SOLD인지와, meId가 실제 구매자인지 검사하는 별도 로직 필요
        if (product.getStatus() != TradeStatus.SOLD) throw new IllegalStateException("거래완료 후 작성할 수 있습니다.");

        User buyer = userRepository.findById(meId).orElseThrow();
        User seller = product.getUser(); // 상품의 판매자

        // 2) 저장
        Review saved = reviewRepository.save(
                Review.builder()
                        .transactionId(req.getTransactionId())
                        .productId(product.getId())
                        .buyer(buyer)
                        .seller(seller)
                        .rating(req.getRating())
                        .content(req.getContent())
                        .build()
        );

        // 3) 매너점수 반영
        double delta = deltaByRating(req.getRating());
        double next = clamp((seller.getMannerTemperature() == null ? 36.5 : seller.getMannerTemperature()) + delta, 0.0, 100.0);
        seller.setMannerTemperature(next);
        userRepository.save(seller);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listReceived(Integer sellerId, Pageable pg) {
        return reviewRepository.findBySeller_Id(sellerId, pg).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listWritten(Integer buyerId, Pageable pg) {
        return reviewRepository.findByBuyer_Id(buyerId, pg).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByProduct(Integer productId, Pageable pg) {
        return reviewRepository.findByProductId(productId, pg).map(this::toResponse);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .transactionId(r.getTransactionId())
                .productId(r.getProductId())
                .buyerId(r.getBuyer().getId())
                .buyerNickname(r.getBuyer().getNickname())
                .sellerId(r.getSeller().getId())
                .sellerNickname(r.getSeller().getNickname())
                .rating(r.getRating())
                .content(r.getContent())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

package com.mrokga.carrot_server.product.service;

import com.mrokga.carrot_server.notification.service.NotificationService;
import com.mrokga.carrot_server.product.dto.request.ChangeStatusRequestDto;
import com.mrokga.carrot_server.product.dto.request.CreateProductRequestDto;
import com.mrokga.carrot_server.product.dto.request.ProductImageRequestDto;
import com.mrokga.carrot_server.product.dto.response.ChangeStatusResponseDto;
import com.mrokga.carrot_server.product.dto.response.ProductDetailResponseDto;
import com.mrokga.carrot_server.product.dto.response.ProductListItemDto;
import com.mrokga.carrot_server.product.entity.*;
import com.mrokga.carrot_server.product.enums.TradeStatus;
import com.mrokga.carrot_server.product.repository.*;
import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.region.entity.UserRegion;
import com.mrokga.carrot_server.region.entity.embeddable.Location;
import com.mrokga.carrot_server.region.repository.RegionRepository;
import com.mrokga.carrot_server.region.repository.UserRegionRepository;
import com.mrokga.carrot_server.transaction.entity.Transaction;
import com.mrokga.carrot_server.transaction.repository.TransactionRepository;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.user.repository.UserRepository;
import com.openai.models.beta.threads.runs.Run;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductExposureRegionRepository productExposureRegionRepository;
    private final UserRegionRepository userRegionRepository;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;

    /**
     * 새로운 상품을 등록
     *
     * @param req 상품 생성에 필요한 데이터 DTO
     * @return 저장된 Product 엔티티
     */
    @Transactional
    public Product createProduct(CreateProductRequestDto req) {
        try {
            log.info("[ProductService.createProduct] req = {}", req);

            // 1) 기본 엔티티 조회 및 유효성 검증
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] User not found"));

            Region region = regionRepository.findById(req.getRegionId())
                    .orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] Region not found"));

            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] Category not found"));

            // 2) 이미지 매핑 및 처리 (순서, 썸네일 지정)
            List<ProductImage> productImages = null;
            if (req.getImages() != null && !req.getImages().isEmpty()) {
                AtomicInteger index = new AtomicInteger(0);
                // 요청에서 썸네일(isThumbnail=true)이 명시적으로 지정되었는지 확인
                boolean hasThumb = req.getImages().stream()
                        .anyMatch(i -> Boolean.TRUE.equals(i.getIsThumbnail()));

                productImages = req.getImages().stream()
                        // sortOrder 기준으로 정렬
                        .sorted(Comparator.comparingInt(ProductImageRequestDto::getSortOrder))
                        .map(imageDto -> ProductImage.builder()
                                .imageUrl(imageDto.getImageUrl())
                                .sortOrder(index.getAndIncrement())
                                .isThumbnail(
                                        hasThumb
                                                // 썸네일이 지정되어 있다면 그 값을 따르고
                                                ? Boolean.TRUE.equals(imageDto.getIsThumbnail())
                                                // 지정이 안 되어 있으면 첫 번째 이미지(index.get() == 1)를 썸네일로 자동 설정
                                                : index.get() == 1
                                )
                                .build())
                        .toList();
            }

            // 3) 희망 거래 위치
            Location preferredLocation = null;
            if (req.getPreferredLocation() != null) {
                preferredLocation = Location.builder()
                        .latitude(req.getPreferredLocation().getLatitude())
                        .longitude(req.getPreferredLocation().getLongitude())
                        .name(req.getPreferredLocation().getName())
                        .build();
            }

            // 4) Product 엔티티 생성
            Product product = Product.builder()
                    .user(user)
                    .region(region)
                    .category(category)
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .isFree(req.getIsFree())
                    .isPriceSuggestable(req.getIsPriceSuggestable())
                    .price(Boolean.TRUE.equals(req.getIsFree()) ? 0 : req.getPrice())
                    .images(productImages)
                    .preferredLocation(preferredLocation)
                    .build();

            // 연관관계 설정 (ProductImage와 Product 맵핑)
            if (productImages != null) {
                productImages.forEach(img -> img.setProduct(product));
            }

            // 5) 저장
            productRepository.save(product);

            // 6) 노출 지역 저장
            if (req.getExposureRegions() != null && !req.getExposureRegions().isEmpty()) {
                List<ProductExposureRegion> exposureRegions = req.getExposureRegions().stream()
                        .map(item -> {

                            Region entity = regionRepository.findByName(item)
                                    .orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] Region not found"));

                            return ProductExposureRegion.builder()
                                    .product(product)
                                    .region(entity)
                                    .build();
                        })
                        .toList();

                productExposureRegionRepository.saveAll(exposureRegions);
            }

            Favorite favorite = favoriteRepository.findByUserIdAndCategoryId(req.getUserId(), req.getCategoryId());

            // 7) 카테고리 알림 전송 (찜한 카테고리 알림)
            if (favorite != null) {
                notificationService.sendCategoryProductNotification(user, product);
            }

            return product;

        } catch (Exception e) {
            log.error("[ProductService.createProduct] error occurred. {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 상품의 거래 상태(TradeStatus)를 변경하고 관련 트랜잭션(Transaction) 정보를 업데이트
     *
     * 판매중: 예약중/거래완료 둘 다 전이가능
     * 판매중 -> 예약중: status 예약중으로 변경, transaction 생성, buyer_id가 null이 아니면 추가
     * 판매중 -> 거래완료: status 거래완료로 변경, transaction 생성, buyer_id가 null이 아니면 추가, completed_at 추가
     *
     * 예약중: 판매중/거래완료 둘 다 전이가능
     * 예약중 -> 판매중: transaction 삭제, status 판매중으로 변경
     * 예약중 -> 거래완료: status 거래완료로 변경, buyer_id가 null이면 삭제, null이 아니면 변경
     *
     * 거래완료 -> 판매중: transaction 삭제, 리뷰가 있을 경우 삭제, status 판매중으로 변경
     *
     * @param req 상태 변경 요청 DTO (상품 ID, 변경할 상태, 구매자 ID 등 포함)
     * @return 변경된 상태 정보를 포함하는 응답 DTO
     * @throws EntityNotFoundException 상품, 판매자, 구매자 또는 트랜잭션을 찾을 수 없을 때 발생
     */
    @Transactional
    public ChangeStatusResponseDto changeStatus(ChangeStatusRequestDto req) {
        log.info("[ProductService.changeStatus] req = {}", req);

        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        User user = userRepository.findById(req.getSellerId()).orElseThrow(() -> new EntityNotFoundException("User not found"));

        TradeStatus status = product.getStatus();

        Transaction transaction = null;

        switch (status) {
            case ON_SALE -> { // 현재: 판매중
                // 판매중 -> 예약중 or 거래완료로 변경 시 새로운 transaction 생성
                transaction = Transaction.builder()
                        .product(product)
                        .seller(user)
                        .build();

                // 요청된 상태로 변경
                product.setStatus(req.getStatus());

                // 요청에 구매자가 지정된 경우 트랜잭션에 구매자 정보 업데이트
                if (req.getBuyerId() != null) {
                    transaction.setBuyer(userRepository.findById(req.getBuyerId()).orElseThrow(() -> new EntityNotFoundException("Buyer not found")));
                }

                // 거래 완료(SOLD)로 변경하는 경우 완료 시간 설정
                if (req.getStatus().equals(TradeStatus.SOLD) && req.getCompletedAt() != null) {
                    transaction.setCompletedAt(req.getCompletedAt());
                }

                transactionRepository.save(transaction);
            }


            case RESERVED -> { // 현재: 예약중

                // product ID로 트랜잭션 조회
                transaction = transactionRepository.findByProductId(req.getProductId()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
                log.info("[ProductService.changeStatus] transaction = {}", transaction);

                if (req.getStatus().equals(TradeStatus.ON_SALE)) {
                    // 판매중으로 변경 시 기존 트랜잭션 삭제
                    transactionRepository.delete(transaction);
                } else if (req.getStatus().equals(TradeStatus.SOLD)) {
                    // 거래완료로 변경 시 구매자 정보 업데이트
                    transaction.setBuyer(userRepository.findById(req.getBuyerId()).orElseThrow(() -> new EntityNotFoundException("Buyer not found")));

                    // 거래 완료 시간 설정
                    if (req.getCompletedAt() != null) {
                        transaction.setCompletedAt(req.getCompletedAt());
                    }
                }

                product.setStatus(req.getStatus());
            }

            case SOLD -> { // 현재: 거래완료
                // 기존 트랜잭션 조회
                transaction = transactionRepository.findByProductId(req.getProductId()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

                // 해당 트랜잭션에 리뷰가 있는 경우 예외처리
                if (reviewRepository.existsByTransactionId(transaction.getId())) {
                    throw new RuntimeException("리뷰가 작성된 트랜잭션은 변경 불가");
                }

                if (req.getStatus().equals(TradeStatus.ON_SALE)) {

                    // 판매중으로 변경 시 기존 트랜잭션 삭제
                    transactionRepository.delete(transaction);

                } else if (req.getStatus().equals(TradeStatus.RESERVED)) {
                    // 구매자 정보 업데이트
                    transaction.setBuyer(userRepository.findById(req.getBuyerId()).orElseThrow(() -> new EntityNotFoundException("Buyer not found")));
                }

                // 기존 거래 완료 시각 삭제
                transaction.setCompletedAt(null);

                product.setStatus(req.getStatus());
            }

            default -> throw new RuntimeException("Invalid status [" + status + "]");

        }

        return ChangeStatusResponseDto.builder()
                .productId(transaction.getProduct().getId())
                .sellerId(transaction.getSeller().getId())
                .buyerId(transaction.getBuyer().getId())
                .status(product.getStatus())
                .completedAt(transaction.getCompletedAt())
                .build();

    }

    /**
     * 상품 상세 정보를 조회하고 조회수 1 증가
     *
     * @param id 조회할 상품의 ID
     * @return 조회된 Product 엔티티
     * @throws EntityNotFoundException 상품을 찾을 수 없을 때 발생
     */
    @Transactional
    public Product getProductDetail(int id) {
        // 모든 연관 관계 entity들을 한 번에 fetch하여 N+1 문제 방지
        Product product = productRepository.findByIdWithAllRelations(id).orElseThrow(() -> new EntityNotFoundException("[ProductService.getProductDetail] Product not found"));

        // 조회수 증가
        product.increaseViewCount();

        return product;
    }

    /**
     * 카테고리 또는 상품에 대한 찜(Favorite) 상태를 토글합니다.
     *
     * @param userId 사용자 ID
     * @param type 찜하기 대상 타입 ('C' - Category, 'P' - Product)
     * @param targetId 대상 엔티티의 ID (Category ID 또는 Product ID)
     * @throws EntityNotFoundException 사용자, 카테고리 또는 상품을 찾을 수 없을 때 발생
     */
    @Transactional
    public void toggleFavorite(int userId, String type, int targetId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("[ProductService.toggleFavorite] User not found"));

        switch (type.toUpperCase()) {
            case "C" -> { // 카테고리 찜하기
                Category category = categoryRepository.findById(targetId).orElseThrow(() -> new EntityNotFoundException("[ProductService.toggleFavorite] Category not found"));

                Favorite favorite = favoriteRepository.findByUserIdAndCategoryId(userId, category.getId());

                // 찜하기 등록
                if (favorite == null) {
                    favoriteRepository.save(Favorite.builder()
                            .category(category)
                            .user(user)
                            .build());

                } else {
                    // 찜하기 취소
                    favoriteRepository.delete(favorite);
                }
            }

            case "P" -> { // 상품 찜하기
                Product product = productRepository.findById(targetId).orElseThrow(() -> new EntityNotFoundException("[ProductService.toggleFavorite] Product not found"));

                Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, targetId);

                // 찜하기 등록 + count 증가
                if (favorite == null) {
                    favoriteRepository.save(Favorite.builder()
                            .product(product)
                            .user(user)
                            .build());

                    product.increaseFavoriteCount();
                } else {
                    // 찜하기 취소 + count 감소
                    favoriteRepository.delete(favorite);
                    product.decreaseFavoriteCount();
                }
            }

            default -> throw new RuntimeException("Invalid favorite type [" + type + "]");
        }
    }

    /**
     * 특정 사용자의 활성 노출 지역을 기준으로 상품 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 지역에 노출 설정된 상품 목록 DTO
     * @throws EntityNotFoundException 활성 사용자 지역 정보를 찾을 수 없을 때 발생
     */
    @Transactional
    public List<ProductDetailResponseDto> getProductList(int userId) {

        // 사용자 ID로 활성(Active) 지역 정보를 조회
        UserRegion userRegion = userRegionRepository.findActiveByUserId(userId).orElseThrow(() -> new EntityNotFoundException("[ProductService.getProductList] UserRegion not found"));

        // 해당 지역에 노출하도록 설정된 상품 목록 조회
        return productRepository.findAllDtoByExposureRegion(userRegion.getRegion());
    }

    /**
     * 상품 제목을 기준으로 키워드 검색을 수행하고 페이지네이션된 결과를 반환합니다.
     *
     * @param keyword 검색할 키워드
     * @param pageable 페이지네이션 정보 (페이지 번호, 크기, 정렬)
     * @return 검색 결과가 담긴 페이지 DTO
     */
    @Transactional(readOnly = true)
    public Page<ProductDetailResponseDto> searchProduct(String keyword, Pageable pageable) {
        // 키워드가 없으면 빈 페이지 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        // 제목에 키워드가 포함된 상품 목록을 페이지네이션하여 조회
        return productRepository.findAllByTitleContaining(keyword, pageable);
    }

    @Transactional
    public void deleteProduct(int productId, int sellerId) {
        // 1) 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 2) 소유자(판매자) 검증
        if (product.getUser() == null || !product.getUser().getId().equals(sellerId)) {
            throw new SecurityException("본인이 등록한 상품만 삭제할 수 있습니다.");
        }

        // (선택) 상태 제한: 판매완료는 삭제 금지하고 싶다면 주석 해제
        // if (product.getStatus() == TradeStatus.SOLD) {
        //     throw new IllegalStateException("거래 완료 상품은 삭제할 수 없습니다.");
        // }

        // 3) 연관 데이터 정리
        transactionRepository.deleteByProduct(product);
        favoriteRepository.deleteByProduct(product);
        productExposureRegionRepository.deleteByProduct(product);
        // 이미지(ProductImage)는 orphanRemoval=true, cascade=ALL 이므로 product 삭제 시 자동 정리

        // 4) 상품 삭제
        productRepository.delete(product);
    }


}

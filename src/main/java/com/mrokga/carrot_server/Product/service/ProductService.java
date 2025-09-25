package com.mrokga.carrot_server.Product.service;

import com.mrokga.carrot_server.Product.dto.request.ChangeStatusRequestDto;
import com.mrokga.carrot_server.Product.dto.request.CreateProductRequestDto;
import com.mrokga.carrot_server.Product.dto.response.ProductDetailResponseDto;
import com.mrokga.carrot_server.Product.entity.*;
import com.mrokga.carrot_server.Product.repository.*;
import com.mrokga.carrot_server.Region.entity.Region;
import com.mrokga.carrot_server.Region.entity.UserRegion;
import com.mrokga.carrot_server.Region.repository.RegionRepository;
import com.mrokga.carrot_server.Region.repository.UserRegionRepository;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.User.repository.UserRepository;
import com.mrokga.carrot_server.Region.entity.embeddable.Location;
import com.mrokga.carrot_server.Product.enums.TradeStatus;
import com.mrokga.carrot_server.Product.entity.Category;
import com.mrokga.carrot_server.Product.repository.CategoryRepository;
import com.mrokga.carrot_server.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Product createProduct(CreateProductRequestDto req) {
        try {

            log.info("[ProductService.createProduct] req = {}", req);

            User user = userRepository.findById(req.getUserId()).orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] User not found"));

            Region region = regionRepository.findById(req.getRegionId()).orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] Region not found"));

            Category category = categoryRepository.findById(req.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] Category not found"));

            AtomicInteger index = new AtomicInteger(0);

            List<ProductImage> productImages = req.getImages() != null ?
                    req.getImages().stream()
                            .map(imageDto -> ProductImage.builder()
                                    .imageUrl(imageDto.getImageUrl())
                                    .sortOrder(index.getAndIncrement())
                                    .build())
                            .toList() : null;

            Location preferredLocation = req.getPreferredLocation() != null ?
                    Location.builder()
                            .latitude(req.getPreferredLocation().getLatitude())
                            .longitude(req.getPreferredLocation().getLongitude())
                            .name(req.getPreferredLocation().getName())
                            .build() : null;

            Product product = Product.builder()
                    .user(user)
                    .region(region)
                    .category(category)
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .isFree(req.getIsFree())
                    .isPriceSuggestable((req.getIsPriceSuggestable()))
                    .price(!req.getIsFree() ? req.getPrice() : 0)
                    .images(productImages)
                    .preferredLocation(preferredLocation)
                    .build();

            if (productImages != null) {
                productImages.forEach(img -> img.setProduct(product));
            }

            productRepository.save(product);

            if (req.getExposureRegions() != null && !req.getExposureRegions().isEmpty()) {
                List<ProductExposureRegion> exposureRegions = req.getExposureRegions().stream()
                        .map(item -> {
                            Region entity = regionRepository.findByFullName(item).orElseThrow(() -> new EntityNotFoundException("[ProductService.createProduct] Region not found"));

                            return ProductExposureRegion.builder()
                                    .product(product)
                                    .region(entity)
                                    .build();
                        })
                        .toList();

                productExposureRegionRepository.saveAll(exposureRegions);
            }

            Favorite favorite = favoriteRepository.findByUserIdAndCategoryId(req.getUserId(), req.getCategoryId());

            if (favorite != null) {
                notificationService.sendCategoryProductNotification(user, product);
            }

            return product;
        } catch (Exception e) {
            log.error("[ProductService.createProduct] error occurred. {}", e.getMessage());
            return null;
        }
    }

    /***
     * 판매중: 예약중/거래완료 둘 다 전이가능
     * 판매중 -> 예약중: status 예약중으로 변경, transaction 생성, buyer_id가 null이 아니면 추가
     * 판매중 -> 거래완료: status 거래완료로 변경, transaction 생성, buyer_id가 null이 아니면 추가, completed_at 추가
     *
     * 예약중: 판매중/거래완료 둘 다 전이가능
     * 예약중 -> 판매중: transaction 삭제, status 판매중으로 변경
     * 예약중 -> 거래완료: status 거래완료로 변경, buyer_id가 null이면 삭제, null이 아니면 변경
     *
     * 거래완료 -> 판매중: transaction 삭제, 리뷰가 있을 경우 삭제, status 판매중으로 변경
     */
    @Transactional
    public void changeStatus(ChangeStatusRequestDto req) {

        Product product = productRepository.findById(req.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        User user = userRepository.findById(req.getSellerId()).orElseThrow(() -> new EntityNotFoundException("User not found"));

        TradeStatus status = product.getStatus();

        switch (status) {
            case ON_SALE -> {

                Transaction transaction = Transaction.builder()
                        .product(product)
                        .seller(user)
                        .build();

                product.setStatus(req.getStatus());

                if (req.getBuyerId() != null) {
                    transaction.setBuyer(userRepository.findById(req.getBuyerId()).orElseThrow(() -> new EntityNotFoundException("Buyer not found")));
                }

                if (req.getStatus().equals(TradeStatus.SOLD) && req.getCompletedAt() != null) {
                    transaction.setCompletedAt(req.getCompletedAt());
                }

                transactionRepository.save(transaction);
            }


            case RESERVED -> {

                Transaction transaction = transactionRepository.findById(req.getProductId()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

                if (req.getStatus().equals(TradeStatus.ON_SALE)) {
                    transactionRepository.delete(transaction);
                } else if (req.getStatus().equals(TradeStatus.SOLD)) {
                    if (req.getBuyerId() == null) {
                        transaction.setBuyer(null);
                    } else {
                        transaction.setBuyer(userRepository.findById(req.getBuyerId()).orElseThrow(() -> new EntityNotFoundException("Buyer not found")));
                    }
                }

                product.setStatus(req.getStatus());
            }

            case SOLD -> {
                Transaction transaction = transactionRepository.findById(req.getProductId()).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

                if (req.getStatus().equals(TradeStatus.ON_SALE)) {
                    transactionRepository.delete(transaction);

                    //TODO 당근에서는 리뷰가 있는 경우에만 리뷰 삭제 경고가 뜸. 어떻게 할지 결정 필요
                    // 당근처럼 하려면 review 유무 확인 후 프론트와 한차례 더 통신 필요
//                    if (transaction.getBuyer() != null) {
//                        Review review = reviewRepository.findBySellerIdAndBuyerId();
//
//                        if (review != null) {
//                            return ~~~
//                        }
//                    }

                    product.setStatus(req.getStatus());
                }

            }

            default -> throw new RuntimeException("Invalid status [" + status + "]");

        }
    }

    @Transactional
    public Product getProductDetail(int id) {
        Product product = productRepository.findByIdWithAllRelations(id).orElseThrow(() -> new EntityNotFoundException("[ProductService.getProductDetail] Product not found"));

        product.increaseViewCount();

        return product;
    }

    @Transactional
    public void toggleFavorite(int userId, String type, int targetId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("[ProductService.toggleFavorite] User not found"));

        switch (type.toUpperCase()) {
            case "C" -> {
                Category category = categoryRepository.findById(targetId).orElseThrow(() -> new EntityNotFoundException("[ProductService.toggleFavorite] Category not found"));

                Favorite favorite = favoriteRepository.findByUserIdAndCategoryId(userId, category.getId());

                if (favorite == null) {
                    favoriteRepository.save(Favorite.builder()
                            .category(category)
                            .user(user)
                            .build());

                } else {
                    favoriteRepository.delete(favorite);
                }
            }

            case "P" -> {
                Product product = productRepository.findById(targetId).orElseThrow(() -> new EntityNotFoundException("[ProductService.toggleFavorite] Product not found"));

                Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, targetId);

                if (favorite == null) {
                    favoriteRepository.save(Favorite.builder()
                            .product(product)
                            .user(user)
                            .build());

                    product.increaseFavoriteCount();
                } else {
                    favoriteRepository.delete(favorite);
                    product.decreaseFavoriteCount();
                }
            }

            default -> throw new RuntimeException("Invalid favorite type [" + type + "]");
        }
    }

    @Transactional
    public List<ProductDetailResponseDto> getProductList(int userId) {

        UserRegion userRegion = userRegionRepository.findActiveByUserId(userId).orElseThrow(() -> new EntityNotFoundException("[ProductService.getProductList] UserRegion not found"));

        return productRepository.findAllDtoByExposureRegion(userRegion.getRegion());
    }

    @Transactional
    public List<ProductDetailResponseDto> searchProduct(String keyword) {
        return productRepository.findAllByTitleContaining(keyword);
    }
}

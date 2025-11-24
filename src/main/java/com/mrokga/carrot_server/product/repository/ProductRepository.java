package com.mrokga.carrot_server.product.repository;

import com.mrokga.carrot_server.product.dto.response.ProductDetailResponseDto;
import com.mrokga.carrot_server.product.dto.response.ProductListItemDto;
import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.region.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
        select distinct p
        from Product p
        join fetch p.user
        join fetch p.region
        join fetch p.category
        left join fetch p.images
        where p.id = :id
    """)
    Optional<Product> findByIdWithAllRelations(@Param("id") int id);

    @Query("""
    SELECT new com.mrokga.carrot_server.product.dto.response.ProductDetailResponseDto(
        p.title, p.description, p.user.nickname, r.name, p.category.name,
        p.price, p.isFree, p.status, p.favoriteCount, p.chatCount, p.createdAt, p.updatedAt,
        (SELECT i.imageUrl
             FROM ProductImage i
             WHERE i.product = p AND i.sortOrder = 0
        )
    )
    FROM ProductExposureRegion per
    JOIN per.product p
    JOIN per.region r
    WHERE r = :region
    """)
    List<ProductDetailResponseDto> findAllDtoByExposureRegion(@Param("region") Region region);

    @Query("""
    SELECT new com.mrokga.carrot_server.product.dto.response.ProductDetailResponseDto(
        p.title, p.description, p.user.nickname, p.category.name,
        p.price, p.isFree, p.status, p.favoriteCount, p.chatCount,
        p.createdAt, p.updatedAt, i.imageUrl
    )
    FROM Product p
    LEFT JOIN ProductImage i ON i.product = p AND i.sortOrder = 0
    WHERE REPLACE(LOWER(p.title), ' ', '')
    LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))
    """)
    Page<ProductDetailResponseDto> findAllByTitleContaining(@Param("keyword") String keyword,
                                                            Pageable pageable);

    @Query("""
SELECT new com.mrokga.carrot_server.product.dto.response.ProductListItemDto(
    p.id,                                    
    p.title, p.description,
    p.user.nickname, r.name, p.category.name,
    p.price, p.isFree, p.status,
    p.favoriteCount, p.chatCount,
    p.createdAt, p.updatedAt,
    (SELECT i.imageUrl FROM ProductImage i
     WHERE i.product = p AND i.sortOrder = 0)
)
FROM ProductExposureRegion per
JOIN per.product p
JOIN per.region r
WHERE r = :region
""")
    List<ProductListItemDto> findAllListItemByExposureRegion(@Param("region") Region region);

    @Query("""
    SELECT new com.mrokga.carrot_server.product.dto.response.ProductListItemDto(
        p.id,                                   
        p.title, p.description,
        p.user.nickname, r.name, c.name,
        p.price, p.isFree, p.status,
        p.favoriteCount, p.chatCount,
        p.createdAt, p.updatedAt,
        i.imageUrl
    )
    FROM Product p
    JOIN p.region r
    JOIN p.category c
    LEFT JOIN ProductImage i ON i.product = p AND i.sortOrder = 0
    WHERE REPLACE(LOWER(p.title), ' ', '')
    LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))
    """)
    Page<ProductListItemDto> findAllListItemByTitleContaining(@Param("keyword") String keyword,
                                                              Pageable pageable);


    // 추가 쿼리 1: 내가 판매하는 물품 (판매자=나)
    @Query("""
SELECT new com.mrokga.carrot_server.mypage.dto.MyProductDto(
    p.id,
    p.title, p.description,
    p.user.nickname, r.name, c.name,
    p.price, p.isFree, p.status,
    p.favoriteCount, p.chatCount,
    p.createdAt, p.updatedAt,
    (SELECT i.imageUrl FROM ProductImage i WHERE i.product = p AND i.sortOrder = 0)
)
FROM Product p
JOIN p.region r
JOIN p.category c
WHERE p.user.id = :sellerId
""")
    Page<com.mrokga.carrot_server.mypage.dto.MyProductDto> findMySellingProducts(@Param("sellerId") Integer sellerId, Pageable pageable);

    // 추가 쿼리 2: id 목록으로 MyProductDto 조회(찜/기타 재사용)
    @Query("""
SELECT new com.mrokga.carrot_server.mypage.dto.MyProductDto(
    p.id,
    p.title, p.description,
    p.user.nickname, r.name, c.name,
    p.price, p.isFree, p.status,
    p.favoriteCount, p.chatCount,
    p.createdAt, p.updatedAt,
    (SELECT i.imageUrl FROM ProductImage i WHERE i.product = p AND i.sortOrder = 0)
)
FROM Product p
JOIN p.region r
JOIN p.category c
WHERE p.id IN :ids
""")
    Page<com.mrokga.carrot_server.mypage.dto.MyProductDto> findMyProductsByIds(@Param("ids") List<Integer> ids, Pageable pageable);



}

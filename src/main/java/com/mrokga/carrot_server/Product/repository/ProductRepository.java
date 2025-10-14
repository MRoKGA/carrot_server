package com.mrokga.carrot_server.Product.repository;

import com.mrokga.carrot_server.Product.dto.response.ProductDetailResponseDto;
import com.mrokga.carrot_server.Product.entity.Product;
import com.mrokga.carrot_server.Region.entity.Region;
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
    SELECT new com.mrokga.carrot_server.Product.dto.response.ProductDetailResponseDto(
        p.id, p.title, p.description, p.user.nickname, r.name, p.category.name,
        p.price, p.isFree, p.status, p.favoriteCount, p.chatCount,
        p.createdAt, p.updatedAt,
        (SELECT i.imageUrl
         FROM ProductImage i
         WHERE i.product = p AND i.sortOrder = 0)
    )
    FROM ProductExposureRegion per
    JOIN per.product p
    JOIN per.region r
    WHERE r = :region
    """)
    List<ProductDetailResponseDto> findAllDtoByExposureRegion(@Param("region") Region region);


    @Query("""
    SELECT new com.mrokga.carrot_server.Product.dto.response.ProductDetailResponseDto(
        p.id, p.title, p.description, p.user.nickname, p.category.name,
        p.price, p.isFree, p.status, p.favoriteCount, p.chatCount, p.region.name,
        p.createdAt, p.updatedAt, i.imageUrl
    )
    FROM Product p
    LEFT JOIN ProductImage i ON i.product = p AND i.sortOrder = 0
    WHERE REPLACE(LOWER(p.title), ' ', '')
    LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))
    """)
    Page<ProductDetailResponseDto> findAllByTitleContaining(@Param("keyword") String keyword,
                                                            Pageable pageable);

}

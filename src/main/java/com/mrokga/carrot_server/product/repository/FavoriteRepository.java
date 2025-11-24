package com.mrokga.carrot_server.product.repository;

import com.mrokga.carrot_server.mypage.dto.MyProductDto;
import com.mrokga.carrot_server.product.entity.Favorite;
import com.mrokga.carrot_server.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    Favorite findByUserIdAndProductId(Integer userId, Integer productId);

    Favorite findByUserIdAndCategoryId(Integer userId, Integer categoryId);

    void deleteByProduct(Product product);

    // ✔ 바로 DTO로 뽑아오기(정렬: 즐겨찾기 생성 최신순)
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
    FROM Favorite f
    JOIN f.product p
    JOIN p.region r
    JOIN p.category c
    WHERE f.user.id = :userId
    ORDER BY f.createdAt DESC
    """)
    Page<MyProductDto> findFavoriteProductDtosByUserId(Integer userId, Pageable pageable);
}

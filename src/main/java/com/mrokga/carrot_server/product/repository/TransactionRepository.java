package com.mrokga.carrot_server.product.repository;

import com.mrokga.carrot_server.mypage.dto.PurchasedItemDto;
import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    void deleteByProduct(Product product);

    // ✔ 내가 구매한 물품(구매자=나) — DTO 프로젝션
    @Query("""
    SELECT new com.mrokga.carrot_server.mypage.dto.PurchasedItemDto(
        t.id,
        p.id,
        p.title,
        p.price, p.isFree, p.status,
        s.id, s.nickname,
        p.createdAt, t.completedAt,
        (SELECT i.imageUrl FROM ProductImage i WHERE i.product = p AND i.sortOrder = 0)
    )
    FROM Transaction t
    JOIN t.product p
    JOIN t.seller s
    WHERE t.buyer.id = :buyerId
    ORDER BY COALESCE(t.completedAt, p.createdAt) DESC
    """)
    Page<PurchasedItemDto> findPurchasedItemsByBuyerId(Integer buyerId, Pageable pageable);
}

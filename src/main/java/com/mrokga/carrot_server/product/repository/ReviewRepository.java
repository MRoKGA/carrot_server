// package com.mrokga.carrot_server.product.repository;
package com.mrokga.carrot_server.product.repository;

import com.mrokga.carrot_server.product.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    boolean existsByTransactionId(Integer txId);
    Page<Review> findBySeller_Id(Integer sellerId, Pageable pg); // 받은 후기
    Page<Review> findByBuyer_Id(Integer buyerId, Pageable pg);   // 내가 쓴 후기
    Page<Review> findByProductId(Integer productId, Pageable pg);
}

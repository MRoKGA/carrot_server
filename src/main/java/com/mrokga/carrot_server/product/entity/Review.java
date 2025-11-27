// package com.mrokga.carrot_server.product.entity;
package com.mrokga.carrot_server.product.entity;

import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"transaction_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 거래(구매) 식별자: 구매내역 DTO에 있던 transactionId */
    @Column(name = "transaction_id", nullable = false)
    private Integer transactionId;

    /** 어떤 상품에 대한 후기인지(조회 편의를 위해 저장) */
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /** 후기 작성자(구매자) */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    /** 후기 대상자(판매자) */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    /** 별점 1~5 */
    @Column(name = "rating", nullable = false)
    private int rating;

    /** 코멘트(옵션) */
    @Column(name = "content", length = 500)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

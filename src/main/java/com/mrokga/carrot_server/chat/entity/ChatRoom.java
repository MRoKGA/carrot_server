package com.mrokga.carrot_server.chat.entity;

import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "buyer_id", "seller_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 상품
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // 구매 희망자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @CreationTimestamp
    @Column(name = "created_at", updatable=false)
    private LocalDateTime createdAt;

    // 채팅방 멤버별 마지막으로 읽은 메세지 ID
    @Column(name = "buyer_last_read_message_id")
    private Integer buyerLastReadMessageId;

    @Column(name = "seller_last_read_message_id")
    private Integer sellerLastReadMessageId;

    // 삭제 시점 식별용 속성들(삭제 시점 메세지 ID, 삭제 시간)
    @Column(name = "buyer_delete_cutoff_message_id")
    private Integer buyerDeleteCutoffMessageId;

    @Column(name = "seller_delete_cutoff_message_id")
    private Integer sellerDeleteCutoffMessageId;

    @Column(name = "buyer_deleted_at")
    private LocalDateTime buyerDeletedAt;

    @Column(name = "seller_deleted_at")
    private LocalDateTime sellerDeletedAt;
}

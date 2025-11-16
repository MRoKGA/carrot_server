package com.mrokga.carrot_server.product.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.region.entity.embeddable.Location;
import com.mrokga.carrot_server.product.enums.TradeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_free", nullable = false)
    private Boolean isFree;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "is_price_suggestable", nullable = false)
    private boolean isPriceSuggestable;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TradeStatus status = TradeStatus.ON_SALE;

    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC") // ← 조회 시 항상 업로드 순서대로
    @JsonManagedReference
    private List<ProductImage> images;

    @Embedded
    private Location preferredLocation;

    @Builder.Default
    @NotNull
    @Column(name = "favorite_count")
    private int favoriteCount = 0;

    @Builder.Default
    @NotNull
    @Column(name = "chat_count")
    private int chatCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = TradeStatus.ON_SALE;
        if (viewCount == null) viewCount = 0;
    }
  
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseFavoriteCount() {
        this.favoriteCount++;
    }

    public void decreaseFavoriteCount() {
        this.favoriteCount--;
    }

    public void increaseChatCount() {
        this.chatCount++;
    }

    public void decreaseChatCount() {
        this.chatCount--;
    }
}

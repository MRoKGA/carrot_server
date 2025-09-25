package com.mrokga.carrot_server.Product.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_product_sort", columnNames={"product_id","sort_order"})
        })
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    @JsonBackReference
    private Product product;

    @Column(nullable=false, length=500) private String imageUrl;
    @Column(name="sort_order", nullable=false) private int sortOrder;

    // 대표 이미지(썸네일) 플래그 추가 권장
    @Builder.Default
    @Column(name="is_thumbnail", nullable=false)
    private boolean isThumbnail = false;
}

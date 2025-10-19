package com.mrokga.carrot_server.community.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_image",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_post_sort", columnNames={"post_id","sort_order"})
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id", nullable=false)
    @JsonBackReference
    private Post post;

    // S3에 업로드된 URL
    @Column(nullable=false, length=500)
    private String imageUrl;

    // 업로드 순서 (0,1,2...)
    @Column(name="sort_order", nullable=false)
    private int sortOrder;

    // 대표 이미지(썸네일) 여부
    @Builder.Default
    @Column(name="is_thumbnail", nullable=false)
    private boolean isThumbnail = false;
}

package com.mrokga.carrot_server.community.entity;

import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "communityPost")
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private PostCategory postCategory;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @NotNull
    @Column(name = "view_count")
    private int viewCount = 0;

    @Builder.Default
    @NotNull
    @Column(name = "like_count")
    private int likeCount = 0;

    @Builder.Default
    @NotNull
    @Column(name = "comment_count")
    private int commentCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Post ↔ PostImage 매핑
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PostImage> images = new ArrayList<>();

    // 편의 메소드
    public void increaseViewCount() { this.viewCount++; }
    public void increaseLikeCount() { this.likeCount++; }
    public void decreaseLikeCount() { this.likeCount--; }
    public void increaseCommentCount() { this.commentCount++; }
    public void decreaseCommentCount() { this.commentCount--; }
}

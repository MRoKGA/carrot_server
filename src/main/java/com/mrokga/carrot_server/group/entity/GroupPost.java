package com.mrokga.carrot_server.group.entity;

import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_post")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupPost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Type type; // TEXT, IMAGE, NOTICE

    @Column(nullable = false, length = 1000) private String content;

    @Column(name = "image_url", length = 255) private String imageUrl;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type { TEXT, IMAGE, NOTICE }
}

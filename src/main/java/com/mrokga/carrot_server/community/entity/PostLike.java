package com.mrokga.carrot_server.community.entity;

import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "communityPostLike",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

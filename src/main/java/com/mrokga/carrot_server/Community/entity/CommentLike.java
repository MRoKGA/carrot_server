package com.mrokga.carrot_server.Community.entity;

import com.mrokga.carrot_server.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "communityCommentLike",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

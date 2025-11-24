package com.mrokga.carrot_server.group.entity;

import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "group_membership",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id","user_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupMembership {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Role role; // OWNER, MANAGER, MEMBER

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { OWNER, MANAGER, MEMBER }
}

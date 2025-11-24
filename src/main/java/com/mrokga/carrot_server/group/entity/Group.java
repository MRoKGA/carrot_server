package com.mrokga.carrot_server.group.entity;

import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_group")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Group {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 40) private String name;

    @Column(nullable = false, length = 500) private String description;

    @Column(name = "cover_image_url", length = 255) private String coverImageUrl;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Visibility visibility; // PUBLIC, PRIVATE

    @Enumerated(EnumType.STRING) @Column(name = "join_policy", nullable = false, length = 20)
    private JoinPolicy joinPolicy; // OPEN, APPROVAL, INVITE_ONLY

    @Column(name = "max_members") private Integer maxMembers;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at") private LocalDateTime updatedAt;

    public enum Visibility { PUBLIC, PRIVATE }
    public enum JoinPolicy { OPEN, APPROVAL, INVITE_ONLY }
}

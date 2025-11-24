package com.mrokga.carrot_server.group.entity;

import com.mrokga.carrot_server.region.entity.embeddable.Location;
import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 60) private String title;
    @Column(nullable = false, length = 400) private String description;

    @Embedded private Location location;

    @Column(name = "start_at", nullable = false) private LocalDateTime startAt;
    @Column(name = "end_at", nullable = false) private LocalDateTime endAt;

    @Column(name = "capacity") private Integer capacity;
    @Column(name = "fee") private Integer fee;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

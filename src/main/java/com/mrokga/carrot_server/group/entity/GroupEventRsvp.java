package com.mrokga.carrot_server.group.entity;

import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "group_event_rsvp",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id","user_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupEventRsvp {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "event_id", nullable = false)
    private GroupEvent event;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status; // GOING, INTERESTED, CANCELLED

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { GOING, INTERESTED, CANCELLED }
}

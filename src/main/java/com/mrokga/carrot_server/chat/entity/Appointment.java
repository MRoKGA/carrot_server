package com.mrokga.carrot_server.chat.entity;

import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import com.mrokga.carrot_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 약속이 걸린 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 제안자 (구매자/판매자 구별 없음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    // 약속 시간
    @Column(name = "meeting_time")
    private LocalDateTime meetingTime;

    // 약속 장소
    @Column(name = "meeting_place")
    private String meetingPlace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status; // PENDING, ACCEPTED, REJECTED, CANCELED

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}

package com.mrokga.carrot_server.Chat.entity;

import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.Chat.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message",
        indexes = {
                // 목록 정렬(시간 순) + 타이브레이커
                @Index(name = "idx_msg_room_created_id", columnList = "chat_room_id, created_at, id"),
                // 방의 마지막 메시지
                @Index(name = "idx_msg_room_id", columnList = "chat_room_id, id"),
                // 방에서 '특정 사용자'의 마지막 메시지
                @Index(name = "idx_msg_room_user_id", columnList = "chat_room_id, sender_id, id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 16)
    private MessageType messageType;

    @Column(name = "message", columnDefinition = "text", nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private ChatMessage parentMessage; // null이면 일반 메시지, 있으면 '답장'

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

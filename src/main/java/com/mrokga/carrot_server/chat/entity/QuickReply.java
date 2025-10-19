package com.mrokga.carrot_server.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quick_reply",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "body_norm"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 사용자에게 보여줄 원문(개행 유지 가능) */
    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    /** 중복 판정을 위한 정규화된 텍스트(개행→공백, 트림, 다중공백 축약) */
    @Column(name = "body_norm", nullable = false, length = 1000)
    private String bodyNorm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

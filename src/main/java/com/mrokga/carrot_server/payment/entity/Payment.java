package com.mrokga.carrot_server.payment.entity;

import com.mrokga.carrot_server.payment.enums.PaymentMethod;
import com.mrokga.carrot_server.payment.enums.PaymentStatus;
import com.mrokga.carrot_server.transaction.entity.Transaction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id @GeneratedValue
    private Integer id;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private int amount;

    private LocalDateTime completedAt;
}

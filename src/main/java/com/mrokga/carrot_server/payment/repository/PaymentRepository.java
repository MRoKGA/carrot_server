package com.mrokga.carrot_server.payment.repository;

import com.mrokga.carrot_server.payment.entity.Payment;
import com.mrokga.carrot_server.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTransaction(Transaction transaction);
}

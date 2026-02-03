// src/main/java/com/mrokga/carrot_server/transaction/service/TransactionService.java
package com.mrokga.carrot_server.transaction.service;

import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.enums.TradeStatus;
import com.mrokga.carrot_server.product.repository.ProductRepository;
import com.mrokga.carrot_server.transaction.entity.Transaction;
import com.mrokga.carrot_server.transaction.repository.TransactionRepository;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository txRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    /** 거래 시작: 상품을 예약중으로 만들고 트랜잭션 생성 */
    @Transactional
    public Transaction start(Integer buyerId, Integer productId) {
        User buyer = userRepo.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("buyer not found"));
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));
        User seller = product.getUser();

        if (product.getStatus() != TradeStatus.ON_SALE) {
            throw new IllegalStateException("상품이 판매중 상태가 아닙니다.");
        }
        if (seller.getId().equals(buyerId)) {
            throw new IllegalStateException("본인 상품은 구매할 수 없습니다.");
        }

        product.setStatus(TradeStatus.RESERVED);
        productRepo.save(product);

        Transaction tx = Transaction.builder()
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .completedAt(null) // 예약 상태
                .build();
        return txRepo.save(tx);
    }

    /** 결제 승인 성공 처리: 상품 판매완료 + 거래 완료시간 기록 */
    @Transactional
    public Transaction approve(Integer txId) {
        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new EntityNotFoundException("transaction not found"));

        if (tx.getCompletedAt() != null) return tx; // 멱등성

        Product p = tx.getProduct();
        p.setStatus(TradeStatus.SOLD);
        productRepo.save(p);

        tx.setCompletedAt(LocalDateTime.now());
        return txRepo.save(tx);
    }

    /** 결제 실패/취소: 상품 다시 판매중으로 */
    @Transactional
    public Transaction cancel(Integer txId) {
        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new EntityNotFoundException("transaction not found"));

        // 이미 완료된 거래는 취소 불가(필요 시 예외정책 변경)
        if (tx.getCompletedAt() != null) {
            throw new IllegalStateException("이미 완료된 거래입니다.");
        }

        Product p = tx.getProduct();
        p.setStatus(TradeStatus.ON_SALE);
        productRepo.save(p);

        // completedAt 은 그대로 null 유지(예약 취소)
        return txRepo.save(tx);
    }

    @Transactional(readOnly = true)
    public Transaction get(Integer txId) {
        return txRepo.findById(txId)
                .orElseThrow(() -> new EntityNotFoundException("transaction not found"));
    }
}

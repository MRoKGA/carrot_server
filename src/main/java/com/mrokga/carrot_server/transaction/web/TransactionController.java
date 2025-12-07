// src/main/java/com/mrokga/carrot_server/transaction/web/TransactionController.java
package com.mrokga.carrot_server.transaction.web;

import com.mrokga.carrot_server.transaction.entity.Transaction;
import com.mrokga.carrot_server.transaction.service.TransactionService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // ★ 추가
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService txService;

    /** 거래 시작(예약) */
    @PostMapping
    public ResponseEntity<?> start(@AuthenticationPrincipal UserDetails principal,
                                   @RequestBody StartReq req) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // TokenProvider.generateToken 에서 subject = user.getId()
        Integer buyerId = Integer.valueOf(principal.getUsername());

        Transaction tx = txService.start(buyerId, req.getProductId());
        return ResponseEntity.ok(new TxRes(tx));
    }

    /** 단건 조회(디버그/확인용) */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        Transaction tx = txService.get(id);
        return ResponseEntity.ok(new TxRes(tx));
    }

    /** 예약 취소(디버그/수동취소용) */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        Transaction tx = txService.cancel(id);
        return ResponseEntity.ok(new TxRes(tx));
    }

    @Data
    public static class StartReq { @NotNull private Integer productId; }

    @Data
    public static class TxRes {
        private Integer id;
        private Integer productId;
        private Integer buyerId;
        private Integer sellerId;
        private String  productStatus;
        private String  completedAt;

        public TxRes(Transaction t) {
            this.id = t.getId();
            this.productId = t.getProduct().getId();
            this.buyerId = t.getBuyer() != null ? t.getBuyer().getId() : null;
            this.sellerId = t.getSeller() != null ? t.getSeller().getId() : null;
            this.productStatus = t.getProduct().getStatus().name();
            this.completedAt = t.getCompletedAt() == null ? null : t.getCompletedAt().toString();
        }
    }
}

package com.mrokga.carrot_server.payment.controller;

import com.mrokga.carrot_server.payment.dto.response.KakaoApproveResponse;
import com.mrokga.carrot_server.payment.dto.response.KakaoCancelResponse;
import com.mrokga.carrot_server.payment.dto.response.KakaoReadyResponse;
import com.mrokga.carrot_server.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Tag(name = "Payment API", description = "결제 관련 API")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 준비
    @GetMapping("/kakao/ready/{transactionId}")
    public KakaoReadyResponse kakaoReady(@PathVariable Integer transactionId) {
        return paymentService.kakaoReadyPayment(transactionId);
    }

    // 결제 성공
    @GetMapping("/kakao/success")
    public KakaoApproveResponse kakaoSuccess(@RequestParam("transactionId") Integer transactionId,
                                             @RequestParam("tid") String tid,
                                             @RequestParam("pg_token") String pgToken) {
        return paymentService.kakaoApprovePayment(transactionId, tid, pgToken);
    }

    // 결제 취소
    @PostMapping("/kakao/cancel")
    public KakaoCancelResponse kakaoCancel(@RequestParam String tid, @RequestParam int amount) {
        return paymentService.kakaoCancelPayment(tid, amount);
    }

    // 결제 실패
    @GetMapping("/kakao/fail")
    public String kakaoFail() {
        return "카카오페이 결제가 실패했습니다.";
    }
}
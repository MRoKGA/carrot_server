package com.mrokga.carrot_server.payment.service;

import com.mrokga.carrot_server.payment.dto.response.KakaoApproveResponse;
import com.mrokga.carrot_server.payment.dto.response.KakaoCancelResponse;
import com.mrokga.carrot_server.payment.dto.response.KakaoReadyResponse;
import com.mrokga.carrot_server.payment.entity.Payment;
import com.mrokga.carrot_server.payment.enums.PaymentMethod;
import com.mrokga.carrot_server.payment.enums.PaymentStatus;
import com.mrokga.carrot_server.payment.repository.PaymentRepository;
import com.mrokga.carrot_server.transaction.entity.Transaction;
import com.mrokga.carrot_server.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    @Value("${kakaopay.host}")
    private String kakaoHost;

    @Value("${kakaopay.cid}")
    private String kakaoCid;

    @Value("${kakaopay.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakaopay.base-url}")
    private String kakaoBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders buildKakaoHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    // ✅ 카카오페이 결제 준비
    public KakaoReadyResponse kakaoReadyPayment(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (paymentRepository.findByTransaction(transaction).isPresent()) {
            throw new IllegalStateException("이미 결제가 진행 중인 거래입니다.");
        }

        String url = kakaoHost + "/v1/payment/ready";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoCid);
        params.add("partner_order_id", String.valueOf(transaction.getId()));
        params.add("partner_user_id", String.valueOf(transaction.getBuyer().getId()));
        params.add("item_name", transaction.getProduct().getTitle());
        params.add("quantity", "1");
        params.add("total_amount", String.valueOf(transaction.getProduct().getPrice()));
        params.add("tax_free_amount", "0");
        params.add("approval_url", kakaoBaseUrl + "/success?transactionId=" + transaction.getId());
        params.add("cancel_url", kakaoBaseUrl + "/cancel");
        params.add("fail_url", kakaoBaseUrl + "/fail");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();

        if (response.getStatusCode().isError() || body == null) {
            throw new IllegalStateException("카카오페이 결제 준비 실패: " + body.get("msg"));
        }

        log.info("[KakaoPay] Ready - transactionId={}, tid={}", transactionId, body.get("tid"));

        Payment payment = Payment.builder()
                .transaction(transaction)
                .method(PaymentMethod.KAKAOPAY)
                .status(PaymentStatus.READY)
                .amount(transaction.getProduct().getPrice())
                .tid((String) body.get("tid"))
                .completedAt(null)
                .build();
        paymentRepository.save(payment);

        return KakaoReadyResponse.builder()
                .tid((String) body.get("tid"))
                .redirectPcUrl((String) body.get("next_redirect_pc_url"))
                .redirectMobileUrl((String) body.get("next_redirect_mobile_url"))
                .readiedAt((String) body.get("created_at"))
                .build();
    }

    // ✅ 카카오페이 결제 승인
    public KakaoApproveResponse kakaoApprovePayment(Integer transactionId, String tid, String pgToken) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        Payment payment = paymentRepository.findByTransaction(transaction)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 결제입니다.");
        }

        String url = kakaoHost + "/v1/payment/approve";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoCid);
        params.add("tid", tid);
        params.add("partner_order_id", String.valueOf(transaction.getId()));
        params.add("partner_user_id", String.valueOf(transaction.getBuyer().getId()));
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();

        int kakaoAmount = (Integer) ((Map<String, Object>) body.get("amount")).get("total");
        int expectedAmount = transaction.getProduct().getPrice();
        if (kakaoAmount != expectedAmount) {
            throw new IllegalStateException("결제 금액 불일치 (expected=" + expectedAmount + ", kakao=" + kakaoAmount + ")");
        }

        log.info("[KakaoPay] Approved - transactionId={}, tid={}", transactionId, tid);

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return KakaoApproveResponse.builder()
                .aid((String) body.get("aid"))
                .tid((String) body.get("tid"))
                .method((String) body.get("payment_method_type"))
                .amount((Integer) ((Map<String, Object>) body.get("amount")).get("total"))
                .approvedAt((String) body.get("approved_at"))
                .build();
    }

    // ✅ 카카오페이 결제 취소
    public KakaoCancelResponse kakaoCancelPayment(String tid, int cancelAmount) {
        String url = kakaoHost + "/v1/payment/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoCid);
        params.add("tid", tid);
        params.add("cancel_amount", String.valueOf(cancelAmount));
        params.add("cancel_tax_free_amount", "0");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();

        Payment payment = paymentRepository.findByTid(tid)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        log.info("[KakaoPay] Canceled - tid={}", tid);

        return KakaoCancelResponse.builder()
                .tid((String) body.get("tid"))
                .canceledAmount((Integer) ((Map<String, Object>) body.get("canceled_amount")).get("total"))
                .canceledAt((String) body.get("canceled_at"))
                .build();
    }
}

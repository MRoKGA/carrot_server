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
import com.mrokga.carrot_server.transaction.service.TransactionService;
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
    private final TransactionService transactionService;

    @Value("${kakaopay.host}")
    private String kakaoHost;

    @Value("${kakaopay.cid}")
    private String kakaoCid;

    @Value("${kakaopay.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakaopay.base-url}")
    private String kakaoBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 카카오페이 API 통신을 위한 공통 헤더 생성 method
     * Authorization header와 Content-Type 설정
     * @return HttpHeaders 객체
     */
    private HttpHeaders buildKakaoHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    /**
     * 카카오페이 결제 준비를 요청하고, DB에 결제 정보를 저장
     * @param transactionId 거래 ID
     * @return 카카오페이 결제 준비 응답 DTO
     */
    public KakaoReadyResponse kakaoReadyPayment(Integer transactionId) {
        // 1. 트랜잭션 조회 및 유효성 검증
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // 2. 이미 결제가 진행 중인지 확인 (중복 요청 방지)
        if (paymentRepository.findByTransaction(transaction).isPresent()) {
            throw new IllegalStateException("이미 결제가 진행 중인 거래입니다.");
        }

        // 3. 카카오페이 결제 준비 API URL
        String url = kakaoHost + "/v1/payment/ready";

        // 4. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 5. 요청 파라미터 설정
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

        // 6. API 요청
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();

        // 7. 응답 확인 및 예외 처리
        if (response.getStatusCode().isError() || body == null) {
            throw new IllegalStateException("카카오페이 결제 준비 실패: " + body.get("msg"));
        }

        log.info("[KakaoPay] Ready - transactionId={}, tid={}", transactionId, body.get("tid"));

        // 8. DB에 결제 정보 저장 (상태: READY)
        Payment payment = Payment.builder()
                .transaction(transaction)
                .method(PaymentMethod.KAKAOPAY)
                .status(PaymentStatus.READY)
                .amount(transaction.getProduct().getPrice())
                .tid((String) body.get("tid"))
                .completedAt(null)
                .build();
        paymentRepository.save(payment);

        // 9. 클라이언트에 Redirect 정보 반환
        return KakaoReadyResponse.builder()
                .tid((String) body.get("tid"))
                .redirectPcUrl((String) body.get("next_redirect_pc_url"))
                .redirectMobileUrl((String) body.get("next_redirect_mobile_url"))
                .readiedAt((String) body.get("created_at"))
                .build();
    }

    /**
     * 카카오페이 결제 승인 요청
     * 결제 금액 검증 후 상태를 APPROVED로 업데이트
     * @param transactionId 거래 ID
     * @param pgToken 결제 승인 요청을 위한 토큰
     * @return 카카오페이 결제 승인 응답 DTO
     */
    public KakaoApproveResponse kakaoApprovePayment(Integer transactionId, String pgToken) {
        // 1. 트랜잭션, 결제 정보 조회 및 유효성 검사
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        Payment payment = paymentRepository.findByTransaction(transaction)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        String tid = payment.getTid();

        // 2. 이미 승인된 결제인지 확인(중복 요청 방지)
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 결제입니다.");
        }

        // 3. 카카오페이 결제 승인 API URL
        String url = kakaoHost + "/v1/payment/approve";

        // 4. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 5. 요청 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoCid);
        params.add("tid", tid);
        params.add("partner_order_id", String.valueOf(transaction.getId()));
        params.add("partner_user_id", String.valueOf(transaction.getBuyer().getId()));
        params.add("pg_token", pgToken);

        // 6. API 요청
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (response.getStatusCode().isError() || body == null) {
            throw new IllegalStateException("카카오페이 승인 실패");
        }

        // 7. 결제 금액 검증
        int kakaoAmount = (Integer) ((Map<String, Object>) body.get("amount")).get("total");
        int expectedAmount = transaction.getProduct().getPrice();
        // 결제 금액이 DB에 기록된 기대 금액과 일치하지 않을 경우 예외 처리
        if (kakaoAmount != expectedAmount) {
            throw new IllegalStateException("결제 금액 불일치 (expected=" + expectedAmount + ", kakao=" + kakaoAmount + ")");
        }

        // 내부 거래 완료 처리 (상품 SOLD)
        transactionService.approve(transactionId);

        // 8. DB 상태 업데이트 (결제 상태: APPROVED)
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 9. 트랜잭션 완료 시간 업데이트
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // 10. 승인 응답 DTO 반환
        return KakaoApproveResponse.builder()
                .aid((String) body.get("aid"))
                .tid((String) body.get("tid"))
                .method((String) body.get("payment_method_type"))
                .amount((Integer) ((Map<String, Object>) body.get("amount")).get("total"))
                .approvedAt((String) body.get("approved_at"))
                .build();
    }

    /**
     * 카카오페이 결제 취소 요청
     * @param tid 카카오페이 거래 고유번호
     * @param cancelAmount 취소할 금액
     * @return 카카오페이 결제 취소 응답 DTO
     */
    public KakaoCancelResponse kakaoCancelPayment(String tid, int cancelAmount) {
        // 1. 카카오페이 결제 취소 API URL
        String url = kakaoHost + "/v1/payment/cancel";

        // 2. 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 3. 요청 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", kakaoCid);
        params.add("tid", tid);
        params.add("cancel_amount", String.valueOf(cancelAmount));
        params.add("cancel_tax_free_amount", "0");

        // 4. API 요청
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (response.getStatusCode().isError() || body == null) {
            throw new IllegalStateException("카카오페이 취소 실패");
        }

        // 5. DB에서 결제 정보 조회 및 상태 업데이트 (상태: CANCELED)
        Payment payment = paymentRepository.findByTid(tid)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(PaymentStatus.CANCELED);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 예약 취소 처리(상품 ON_SALE 복귀)
        Transaction tx = payment.getTransaction();
        if (tx != null) {
            transactionService.cancel(tx.getId());
        }

        // 6. 취소 응답 DTO 반환
        return KakaoCancelResponse.builder()
                .tid((String) body.get("tid"))
                .canceledAmount((Integer) ((Map<String, Object>) body.get("canceled_amount")).get("total"))
                .canceledAt((String) body.get("canceled_at"))
                .build();
    }
}

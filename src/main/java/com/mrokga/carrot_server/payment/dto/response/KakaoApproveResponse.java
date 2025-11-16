package com.mrokga.carrot_server.payment.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoApproveResponse {
    private String aid;
    private String tid;
    private String method;
    private int amount;
    private String approvedAt;
}
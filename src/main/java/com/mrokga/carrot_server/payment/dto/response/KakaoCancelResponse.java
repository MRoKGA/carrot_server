package com.mrokga.carrot_server.payment.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoCancelResponse {
    private String tid;
    private int canceledAmount;
    private String canceledAt;
}
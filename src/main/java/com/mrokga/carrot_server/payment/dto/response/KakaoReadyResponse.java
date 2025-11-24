package com.mrokga.carrot_server.payment.dto.response;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoReadyResponse {
    private String tid;
    private String redirectPcUrl;
    private String redirectMobileUrl;
    private String readiedAt;
}
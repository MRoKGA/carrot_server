package com.mrokga.carrot_server.Auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerifyCodeResult {
    OK("인증 성공"),
    MISMATCH("인증코드 불일치"),
    EXPIRED("인증코드 만료");

    private final String message;
}

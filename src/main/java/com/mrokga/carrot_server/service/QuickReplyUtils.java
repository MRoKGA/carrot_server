package com.mrokga.carrot_server.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class QuickReplyUtils {
    private QuickReplyUtils() {}

    /* -------------------- 현재 사용자 ID -------------------- */

    /** SecurityContext에서 Integer 형태의 userId를 뽑아온다. (프로젝트 Principal에 맞게 반영됨) */
    public static Integer currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");

        Object principal = auth.getPrincipal();
        // 1) principal.getId() 리플렉션 시도 (CustomUserPrincipal, JwtUser 등)
        try {
            Method m = principal.getClass().getMethod("getId");
            Object v = m.invoke(principal);
            if (v instanceof Number n) return n.intValue();
            return Integer.valueOf(String.valueOf(v));
        } catch (Exception ignore) { /* fallback */ }

        // 2) auth.getName()이 숫자라면 userId로 간주
        String name = auth.getName();
        try {
            return Integer.valueOf(name);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot resolve current user id");
        }
    }

    /* -------------------- 텍스트 정규화 -------------------- */

    /** 제로폭 문자 제거(보이지 않는 공백류) */
    private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B\\u200C\\u200D\\uFEFF]");
    /** 모든 공백(개행 포함) */
    private static final Pattern ANY_WS = Pattern.compile("\\s+");


    public static String normalizeForDuplicate(String raw) { // 메서드명 유지
        if (raw == null) return "";
        // 1) 유니코드 호환 정규화
        String s = Normalizer.normalize(raw, Normalizer.Form.NFKC);
        // 2) 제로폭 제거
        s = ZERO_WIDTH.matcher(s).replaceAll("");
        // 3) 개행 -> 공백
        s = s.replace('\r', ' ').replace('\n', ' ');
        // 4) 모든 공백 제거
        s = ANY_WS.matcher(s).replaceAll("");
        // 5) 소문자
        s = s.toLowerCase(Locale.ROOT);
        // 6) trim
        return s.trim();
    }
}

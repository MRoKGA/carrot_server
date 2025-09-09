package com.mrokga.carrot_server.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
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

    /** 다중 공백을 1개로, 앞뒤 공백 제거. 개행은 1칸 공백으로 치환(중복 판정용). */
    private static final Pattern MULTI_WS = Pattern.compile("\\s+");

    /** 예: "안녕하세요  \n  반가워요" → "안녕하세요 반가워요" */
    public static String normalizeForDuplicate(String raw) {
        if (raw == null) return "";
        String noNewlines = raw.replace('\n', ' ').replace('\r', ' ');
        String trimmed = noNewlines.trim();
        return MULTI_WS.matcher(trimmed).replaceAll(" ");
    }
}

package com.mrokga.carrot_server.gpt.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.gpt.logging.RingBufferAppender;
import com.mrokga.carrot_server.util.SafeRedactor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ErrorExplainService {

    private final String apiKey;
    private final String model;
    private final boolean enabled;

    private final AtomicLong lastCall = new AtomicLong(0);
    private static final long COOLDOWN_MS = 60_000;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public ErrorExplainService(
            @Value("${OPENAI_API_KEY}") String apiKey,
            @Value("${OPENAI_MODEL:gpt-4o-mini}") String model,
            @Value("${GPT_ERROR_REPORT_ENABLED:true}") boolean enabled
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled && apiKey != null && !apiKey.isBlank();
    }

    public String explain(Throwable t) {
        if (!enabled) return "(GPT 리포트 비활성화됨)";
        long now = System.currentTimeMillis();
        if (now - lastCall.get() < COOLDOWN_MS) return "(쿨다운 중: 잠시 후 재시도)";
        lastCall.set(now);

        String stack = SafeRedactor.redact(stackTrace(t));
        String logs  = SafeRedactor.redact(RingBufferAppender.tail());

        String prompt = """
            역할: Spring Boot + MySQL 백엔드 SRE.
            아래 정보로 에러 원인을 분석하고 한국어로:

            [요약] 한 줄 진단
            [원인] 가장 유력한 원인 3가지
            [조치] 바로 적용 가능한 해결 체크리스트 (번호 매김)
            [다음] 재발 방지 팁 3가지

            # 예외 스택
            ```text
            %s
            ```

            # 최근 로그 tail
            ```text
            %s
            ```

            환경: JVM 17 / Windows, 프레임워크: Spring Boot.
            답변은 간결한 불릿 위주로.
            """.formatted(stack, logs);

        try {
            // Chat Completions 요청 바디 구성
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("temperature", 0.1);
            body.put("max_tokens", 700);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are an expert JVM troubleshooter."));
            messages.add(Map.of("role", "user", "content", prompt));
            body.put("messages", messages);

            String json = mapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                return "(OpenAI 호출 실패) HTTP " + resp.statusCode() + " : " + resp.body();
            }

            JsonNode root = mapper.readTree(resp.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) content = "(응답 본문 파싱 실패)\n" + resp.body();
            return content;

        } catch (Exception e) {
            return "(OpenAI 호출 예외) " + e.getMessage();
        }
    }

    private static String stackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t).append("\n");
        for (StackTraceElement el : t.getStackTrace()) {
            sb.append("  at ").append(el).append("\n");
            if (sb.length() > 30_000) break; // 너무 길면 컷
        }
        Throwable c = t.getCause();
        if (c != null) sb.append("Caused by: ").append(c).append("\n");
        sb.append("CapturedAt=").append(Instant.now());
        return sb.toString();
    }
}

package com.mrokga.carrot_server.GPT.web;

import com.mrokga.carrot_server.GPT.ai.ErrorExplainService;
import com.mrokga.carrot_server.GPT.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorExplainService gpt;

    @Value("${gpt.error-report.expose-to-client:false}")
    private boolean exposeToClient;

    public GlobalExceptionHandler(ErrorExplainService gpt) { this.gpt = gpt; }

    @Hidden
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<?> handle(Exception e) {
        String analysis = gpt.explain(e);
        log.error("\n=== GPT 에러 분석 ===\n{}\n====================", analysis, e);

        if (exposeToClient) {
            return ResponseEntity.status(500).body(analysis);
        } else {
            var body = new ErrorResponse(
                    "서버 내부 오류가 발생했습니다.", 500, java.util.UUID.randomUUID().toString()
            );
            return ResponseEntity.status(500).body(body);
        }
    }
}

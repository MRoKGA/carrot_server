package com.mrokga.carrot_server.gpt.web;

import com.mrokga.carrot_server.gpt.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorTestController {

    @Operation(summary = "의도적으로 500 오류 발생",
            description = "GPT 에러 분석/로그 파이프라인 테스트용 엔드포인트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (표준 에러 응답)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/__test-error")
    public String boom() {
        String x = null; x.length(); // 의도적 NPE
        return "won't reach";
    }
}

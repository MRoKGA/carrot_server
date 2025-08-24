package com.mrokga.carrot_server.GPT.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "표준 에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 메시지", example = "서버 내부 오류가 발생했습니다.")
        String message,
        @Schema(description = "HTTP 상태 코드", example = "500")
        int status,
        @Schema(description = "에러 추적 ID", example = "3a1e2b34-5f6a-7890-1234-56c7890d1e2f")
        String errorId
) {}
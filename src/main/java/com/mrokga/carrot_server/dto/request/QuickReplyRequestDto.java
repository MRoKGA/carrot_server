package com.mrokga.carrot_server.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "자주 쓰는 문구 추가 요청 DTO")
public class QuickReplyRequestDto {
    @NotNull
    @Schema(description = "자주 쓰는 문구로 저장할 채팅 메시지 ID", example = "1")
    private Integer messageId;
}

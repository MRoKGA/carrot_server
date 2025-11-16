package com.mrokga.carrot_server.chat.dto.response;

import com.mrokga.carrot_server.chat.entity.QuickReply;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "자주 쓰는 문구 단건 응답 DTO")
public class QuickReplyItemResponseDto {
    @Schema(description = "메세지 ID", example = "123")
    private Integer id;

    @Schema(description = "원문(개행/공백 포함 그대로 표시용)", example = "안녕하세요! 오늘 7시에 가능하실까요?")
    private String body;

    @Schema(description = "생성 시각(ISO-8601)", example = "2025-08-31T14:23:11")
    private LocalDateTime createdAt;

    public static QuickReplyItemResponseDto from(QuickReply q) {
        return new QuickReplyItemResponseDto(
                q.getId(),
                q.getBody(),
                q.getCreatedAt() == null ? null : q.getCreatedAt()
        );
    }
}

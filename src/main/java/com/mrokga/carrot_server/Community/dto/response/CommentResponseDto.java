package com.mrokga.carrot_server.Community.dto.response;

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
public class CommentResponseDto {
    @Schema(description = "댓글 ID", example = "20")
    private Integer id;

    @Schema(description = "작성자 ID", example = "2")
    private Integer userId;

    @Schema(description = "작성자 닉네임", example = "이의준")
    private String nickname;

    @Schema(description = "댓글 내용", example = "저는 OO카페 추천합니다!")
    private String content;

    @Schema(description = "댓글 좋아요 수", example = "4")
    private int likeCount;

    @Schema(description = "내가 좋아요 눌렀는지 여부", example = "true")
    private boolean likedByMe;

    @Schema(description = "작성일시", example = "2025-09-23T11:00:00")
    private LocalDateTime createdAt;
}

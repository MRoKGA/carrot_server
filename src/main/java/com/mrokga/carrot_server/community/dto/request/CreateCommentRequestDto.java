package com.mrokga.carrot_server.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequestDto {

    @Schema(description = "게시글 ID", example = "5")
    private Integer postId;

    @Schema(description = "작성자 ID", example = "2")
    private Integer userId;

    @Schema(description = "댓글 내용", example = "저는 OO카페 추천합니다!")
    private String content;
}
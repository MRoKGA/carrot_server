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
public class EditCommentRequestDto {
    @Schema(description = "수정할 댓글 ID", example = "1")
    private Integer id;

    @Schema(description = "게시글 ID", example = "1")
    private Integer postId;

    @Schema(description = "수정할 댓글 내용", example = "공감합니다.")
    private String content;
}

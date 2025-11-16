package com.mrokga.carrot_server.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditPostRequestDto {

    @Schema(description = "게시글 ID", example = "5")
    private Integer postId;

    @Schema(description = "카테고리 ID", example = "3")
    private Integer categoryId;

    @Schema(description = "수정할 제목", example = "동네 카페 다시 추천해주세요!")
    private String title;

    @Schema(description = "수정할 내용", example = "주말 저녁에도 운영하는 카페 있으면 알려주세요~")
    private String content;

    @Schema(description = "게시글 이미지 리스트 (업로드된 S3 URL, 순서 포함)")
    private List<PostImageRequestDto> images;
}
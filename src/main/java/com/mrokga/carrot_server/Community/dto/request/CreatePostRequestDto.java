package com.mrokga.carrot_server.Community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequestDto {
    @Schema(description = "작성자 ID", example = "1")
    private Integer userId;

    @Schema(description = "지역 ID", example = "10")
    private Integer regionId;

    @Schema(description = "카테고리 ID", example = "3")
    private Integer categoryId;

    @Schema(description = "게시글 제목", example = "동네 카페 추천해주세요!")
    private String title;

    @Schema(description = "게시글 내용", example = "주말에 공부할 조용한 카페 찾습니다~")
    private String content;

    @Schema(description = "게시글 이미지 리스트 (업로드된 S3 URL, 순서 포함)")
    private List<PostImageRequestDto> images;
}

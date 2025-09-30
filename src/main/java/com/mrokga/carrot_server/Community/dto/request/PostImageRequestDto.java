package com.mrokga.carrot_server.Community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImageRequestDto {

    @Schema(description = "이미지 URL (S3 업로드된 경로)", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/posts/uuid.jpg")
    private String imageUrl;

    @Schema(description = "이미지 순서 (0부터 시작)", example = "0")
    private Integer sortOrder;

    @Schema(description = "대표 이미지 여부", example = "true")
    private Boolean isThumbnail;
}

package com.mrokga.carrot_server.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(name = "GroupPostCreateRequest", description = "그룹 글 작성 요청")
public class GroupPostCreateRequest {
    @Schema(description = "타입", example = "TEXT", allowableValues = {"TEXT","IMAGE","NOTICE"})
    private String type;

    @Schema(description = "내용", example = "이번 주는 일요일에만 진행합니다!")
    private String content;

    @Schema(description = "이미지 URL(옵션)", example = "https://cdn.example.com/posts/abc.jpg")
    private String imageUrl;
}

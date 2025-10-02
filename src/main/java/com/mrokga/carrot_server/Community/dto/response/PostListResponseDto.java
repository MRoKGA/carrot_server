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
public class PostListResponseDto {

    @Schema(description = "게시글 ID", example = "5")
    private Integer id;

    @Schema(description = "제목", example = "동네 카페 추천해주세요!")
    private String title;

    @Schema(description = "내용 요약 (앞부분만 잘라서)", example = "주말에 공부할 조용한 카페 찾습니다~")
    private String contentPreview;

    @Schema(description = "카테고리명", example = "일상")
    private String categoryName;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "조회수", example = "100")
    private int viewCount;

    @Schema(description = "좋아요 수", example = "12")
    private int likeCount;

    @Schema(description = "댓글 수", example = "3")
    private int commentCount;

    @Schema(description = "작성일시", example = "2025-09-23T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "썸네일 이미지 URL (없으면 null)")
    private String thumbnailUrl;
}

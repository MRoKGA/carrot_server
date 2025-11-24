package com.mrokga.carrot_server.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponseDto {

    @Schema(description = "게시글 ID", example = "5")
    private Integer id;

    @Schema(description = "작성자 ID", example = "1")
    private Integer userId;

    @Schema(description = "작성자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "카테고리명", example = "일상")
    private String category;

    @Schema(description = "지역 ID", example = "10")
    private Integer regionId;

    @Schema(description = "제목", example = "동네 카페 추천해주세요!")
    private String title;

    @Schema(description = "내용", example = "주말에 공부할 조용한 카페 찾습니다~")
    private String content;

    @Schema(description = "조회수", example = "100")
    private int viewCount;

    @Schema(description = "좋아요 수", example = "12")
    private int likeCount;

    @Schema(description = "댓글 수", example = "3")
    private int commentCount;

    @Schema(description = "내가 좋아요 눌렀는지 여부", example = "false")
    private boolean likedByMe;

    @Schema(description = "작성일시", example = "2025-09-23T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-09-23T11:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "댓글 리스트")
    private List<CommentResponseDto> comments;

    @Schema(description = "게시글 이미지 URL 리스트")
    private List<String> imageUrls;

    @Schema(description = "거래 희망 장소(선택)", example = "회기역 1번 출구", nullable = true)
    private String dealPlace;

    @Schema(description = "Lat", nullable = true)
    private Double dealPlaceLat;

    @Schema(description = "Lng", nullable = true)
    private Double dealPlaceLng;
}

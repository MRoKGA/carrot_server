package com.mrokga.carrot_server.Community.controller;

import com.mrokga.carrot_server.Community.dto.request.CreateCommentRequestDto;
import com.mrokga.carrot_server.Community.dto.request.EditCommentRequestDto;
import com.mrokga.carrot_server.Community.service.CommentService;
import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@Tag(name = "Community Comment API", description = "동네생활게시판 댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    private Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        return Integer.valueOf(auth.getName());
    }

    @PostMapping
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public ResponseEntity<ApiResponseDto<?>> createComment(@RequestBody CreateCommentRequestDto dto) {
        dto.setUserId(getCurrentUserId());
        commentService.createComment(dto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "작성자가 댓글을 수정합니다.")
    public ResponseEntity<ApiResponseDto<?>> editComment(
            @PathVariable Integer commentId,
            @RequestBody EditCommentRequestDto dto) {
        dto.setId(commentId);
        commentService.editComment(dto, getCurrentUserId());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "작성자가 댓글을 삭제합니다.")
    public ResponseEntity<ApiResponseDto<?>> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요 토글", description = "댓글 좋아요를 추가/취소합니다.")
    public ResponseEntity<ApiResponseDto<?>> toggleCommentLike(@PathVariable Integer commentId) {
        commentService.toggleCommentLike(getCurrentUserId(), commentId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }
}

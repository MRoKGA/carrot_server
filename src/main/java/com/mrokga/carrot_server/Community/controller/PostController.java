package com.mrokga.carrot_server.Community.controller;

import com.mrokga.carrot_server.Community.dto.request.CreatePostRequestDto;
import com.mrokga.carrot_server.Community.dto.request.EditPostRequestDto;
import com.mrokga.carrot_server.Community.dto.response.PostDetailResponseDto;
import com.mrokga.carrot_server.Community.dto.response.PostListResponseDto;
import com.mrokga.carrot_server.Community.service.PostService;
import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
@Tag(name = "Community Post API", description = "동네생활게시판 게시글 관련 API")
public class PostController {

    private final PostService postService;

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
    @Operation(summary = "게시글 작성", description = "사용자가 새로운 게시글을 작성합니다.")
    public ResponseEntity<ApiResponseDto<?>> createPost(@RequestBody CreatePostRequestDto dto){
        dto.setUserId(getCurrentUserId());
        postService.createPost(dto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success",null));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "사용자가 본인이 작성했던 게시글을 수정합니다.")
    public ResponseEntity<ApiResponseDto<?>> editPost(@PathVariable Integer postId,
                                                      @RequestBody EditPostRequestDto dto){
        dto.setPostId(postId);
        postService.editPost(dto, getCurrentUserId());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "작성자가 게시글을 삭제합니다.")
    public ResponseEntity<ApiResponseDto<?>> deletePost(@PathVariable Integer postId) {
        postService.deletePost(postId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회(지역, 카테고리, 검색키워드)", description = "특정 지역의 게시글 목록을 페이징 조회합니다. 카테고리/검색 키워드가 있으면 필터링합니다.")
    public ResponseEntity<ApiResponseDto<Page<PostListResponseDto>>> getPostList(
            @Parameter(description = "지역 ID", example = "10") @RequestParam Integer regionId,
            @Parameter(description = "카테고리 ID", example = "2") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "검색 키워드", example = "맛집") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<PostListResponseDto> result = postService.getPostList(regionId, categoryId, keyword, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", result));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보와 댓글을 조회합니다.")
    public ResponseEntity<ApiResponseDto<PostDetailResponseDto>> getPostDetail(@PathVariable Integer postId) {
        PostDetailResponseDto result = postService.getPostDetail(postId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", result));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 토글", description = "게시글 좋아요를 추가/취소합니다.")
    public ResponseEntity<ApiResponseDto<?>> togglePostLike(@PathVariable Integer postId) {
        postService.togglePostLike(postId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
    }
}

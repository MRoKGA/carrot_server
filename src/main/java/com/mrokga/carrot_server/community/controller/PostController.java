package com.mrokga.carrot_server.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrokga.carrot_server.aws.service.AwsS3Service;
import com.mrokga.carrot_server.community.dto.request.CreatePostRequestDto;
import com.mrokga.carrot_server.community.dto.request.EditPostRequestDto;
import com.mrokga.carrot_server.community.dto.request.PostImageRequestDto;
import com.mrokga.carrot_server.community.dto.response.PostDetailResponseDto;
import com.mrokga.carrot_server.community.dto.response.PostListResponseDto;
import com.mrokga.carrot_server.community.service.PostService;
import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
@Tag(name = "Community Post API", description = "동네생활게시판 게시글 관련 API")
public class PostController {

    private final PostService postService;
    private final AwsS3Service awsS3Service;

    private Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        return Integer.valueOf(auth.getName());
    }

    // ✅ 2-Step 방식 (JSON-only)
    @PostMapping
    @Operation(summary = "게시글 작성", description = "사용자가 새로운 게시글을 작성합니다.")
    public ResponseEntity<ApiResponseDto<?>> createPost(@RequestBody CreatePostRequestDto dto){
        dto.setUserId(getCurrentUserId());
        postService.createPost(dto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success",null));
    }

    // ✅ 1-Step 방식 (멀티파트)
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "게시글 작성(멀티파트: JSON + 이미지 파일)",
            description = """
            폼 파트:
            - meta: application/json (CreatePostRequestDto)
            - images: file[] (여러 장 가능)
            업로드된 이미지는 S3 URL로 변환되어 meta.images에 자동 주입됩니다.
            """
    )
    public ResponseEntity<ApiResponseDto<?>> createMultipart(
            @RequestPart("meta") String metaJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {
        // 1) JSON 문자열 → DTO 변환
        CreatePostRequestDto dto = new ObjectMapper().readValue(metaJson, CreatePostRequestDto.class);
        dto.setUserId(getCurrentUserId());

        // 2) 파일이 있으면 업로드 후 DTO에 이미지 리스트 세팅
        if (images != null && !images.isEmpty()) {
            List<String> urls = awsS3Service.uploadFile(images);

            AtomicInteger idx = new AtomicInteger(0);
            List<PostImageRequestDto> imageDtos = urls.stream()
                    .map(url -> PostImageRequestDto.builder()
                            .imageUrl(url)
                            .sortOrder(idx.get())
                            .isThumbnail(idx.get() == 0) // 첫 번째 이미지를 썸네일
                            .build())
                    .peek(x -> idx.incrementAndGet())
                    .toList();

            dto.setImages(imageDtos);
        } else {
            dto.setImages(null);
        }

        // 3) 서비스 호출
        postService.createPost(dto);

        // 4) 응답
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(), "success", null));
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

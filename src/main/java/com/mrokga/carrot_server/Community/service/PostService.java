package com.mrokga.carrot_server.Community.service;

import com.mrokga.carrot_server.Aws.Service.AwsS3Service;
import com.mrokga.carrot_server.Community.dto.request.CreatePostRequestDto;
import com.mrokga.carrot_server.Community.dto.request.EditPostRequestDto;
import com.mrokga.carrot_server.Community.dto.response.CommentResponseDto;
import com.mrokga.carrot_server.Community.dto.response.PostDetailResponseDto;
import com.mrokga.carrot_server.Community.dto.response.PostListResponseDto;
import com.mrokga.carrot_server.Community.entity.*;
import com.mrokga.carrot_server.Community.repository.*;
import com.mrokga.carrot_server.Region.entity.Region;
import com.mrokga.carrot_server.Region.repository.RegionRepository;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final AwsS3Service awsS3Service;

    // 게시글 작성
    public void createPost(CreatePostRequestDto dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("PostService.createPost(): 유저 없음"));

        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new EntityNotFoundException("PostService.createPost(): 지역 없음"));

        PostCategory postCategory = postCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("PostService.createPost(): 카테고리 없음"));

        // 1. 게시글 엔티티 생성
        Post post = Post.builder()
                .user(user)
                .region(region)
                .postCategory(postCategory)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        // 2. 이미지가 있으면 PostImage 엔티티로 변환 후 매핑
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<PostImage> postImages = dto.getImages().stream()
                    .map(imgDto -> PostImage.builder()
                            .post(post)
                            .imageUrl(imgDto.getImageUrl())
                            .sortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : 0)
                            .isThumbnail(imgDto.getIsThumbnail() != null && imgDto.getIsThumbnail())
                            .build()
                    )
                    .toList();

            // Post ↔ PostImage 양방향 매핑
            post.getImages().addAll(postImages);
        }

        postRepository.save(post);
    }

    // 게시글 수정
    public void editPost(EditPostRequestDto dto, Integer me){
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("PostService.editPost(): 게시글 없음"));

        if(!post.getUser().getId().equals(me)){
            throw new SecurityException("PostService.editPost(): 수정 권한이 없음.");
        }

        PostCategory postCategory = postCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("PostService.editPost(): 카테고리 없음"));

        post.setPostCategory(postCategory);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        // ✅ 이미지 교체 로직
        if (dto.getImages() != null) {
            // 기존 이미지들
            List<PostImage> oldImages = post.getImages();

            // 새 이미지 URL들
            List<String> newUrls = dto.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .toList();

            // 삭제될 이미지 추출 = old(DB) list 에는 있는데 new list 에는 없음
            List<PostImage> toDelete = oldImages.stream()
                    .filter(img -> !newUrls.contains(img.getImageUrl()))
                    .toList();

            // 삭제 처리 (DB + S3)
            toDelete.forEach(img -> {
                awsS3Service.deleteFileByUrl(img.getImageUrl());
                post.getImages().remove(img);
            });

            // 새로 추가된 이미지 = new list 에는 있는데 old(DB) list 에는 없음
            dto.getImages().forEach(imgDto -> {
                boolean exists = oldImages.stream()
                        .anyMatch(img -> img.getImageUrl().equals(imgDto.getImageUrl()));
                if (!exists) {
                    PostImage newImg = PostImage.builder()
                            .post(post)
                            .imageUrl(imgDto.getImageUrl())
                            .sortOrder(imgDto.getSortOrder() != null ? imgDto.getSortOrder() : 0)
                            .isThumbnail(Boolean.TRUE.equals(imgDto.getIsThumbnail()))
                            .build();
                    post.getImages().add(newImg);
                }
            });
        }
    }

    // 게시글 삭제
    public void deletePost(Integer postId, Integer me){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("PostService.deletePost(): 게시글 없음"));

        if(!post.getUser().getId().equals(me)){
            throw new SecurityException("PostService.deletePost(): 삭제 권한 없음");
        }

        // ✅ S3에서 이미지 삭제
        post.getImages().forEach(img -> awsS3Service.deleteFileByUrl(img.getImageUrl()));

        // 해당 게시글의 댓글 좋아요 삭제
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        commentLikeRepository.deleteByCommentIn(comments);

        // 해당 게시글 댓글 삭제
        commentRepository.deleteAll(comments);

        // 해당 게시글 좋아요 삭제
        postLikeRepository.deleteAll(
                postLikeRepository.findAllByPostId(postId)
        );

        // 해당 게시글 삭제
        postRepository.delete(post);
    }

    // 게시글 목록 조회(페이징)
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getPostList(Integer regionId, Integer categoryId, String keyword, Pageable pageable){
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new EntityNotFoundException("PostService.getPostList(): 지역 없음"));

        PostCategory category = null;
        if (categoryId != null) {
            category = postCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("PostService.getPostList(): 카테고리 없음"));
        }

        Page<Post> posts = postRepository.findByRegionAndOptionalCategoryAndKeyword(region, category, keyword, pageable);

        return posts.map(post -> PostListResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .contentPreview(post.getContent().length() > 50
                        ? post.getContent().substring(0, 50) + "..."
                        : post.getContent())
                .categoryName(post.getPostCategory().getName())
                .nickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .thumbnailUrl(
                        post.getImages().stream()
                                .filter(PostImage::isThumbnail)
                                .map(PostImage::getImageUrl)
                                .findFirst()
                                .orElse(null)
                )
                .build());
    }

    // 게시글 상세 조회
    public PostDetailResponseDto getPostDetail(Integer postId, Integer me){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("PostService.getPostDetail(): 게시글 없음"));

        // 조회수 1업
        post.increaseViewCount();

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        // 내가 좋아요 누른 댓글 ID들을 한 번에 조회
        List<Integer> commentIds = comments.stream().map(Comment::getId).toList();
        List<Integer> likedIds = commentLikeRepository.findLikedCommentIds(me, commentIds);
        Set<Integer> likedIdSet = likedIds.stream().collect(Collectors.toSet());

        List<CommentResponseDto> commentDtos = comments.stream()
                .map(c -> CommentResponseDto.builder()
                        .id(c.getId())
                        .userId(c.getUser().getId())
                        .nickname(c.getUser().getNickname())
                        .content(c.getContent())
                        .likeCount(c.getLikeCount())
                        .likedByMe(likedIdSet.contains(c.getId()))
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        boolean likedByMe = postLikeRepository.findByUserIdAndPostId(me, postId) != null;

        return PostDetailResponseDto.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .nickname(post.getUser().getNickname())
                .category(post.getPostCategory().getName())
                .title(post.getTitle())
                .regionId(post.getRegion().getId())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByMe(likedByMe)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(commentDtos)
                .imageUrls(
                        post.getImages().stream()
                                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                                .map(PostImage::getImageUrl)
                                .toList()
                )
                .build();
    }

    // 게시글 좋아요 토글
    public void togglePostLike(Integer postId, Integer me){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("PostService.togglePostLike(): 게시글 없음"));

        User user = userRepository.findById(me)
                .orElseThrow(() -> new EntityNotFoundException("PostService.togglePostLike(): 유저 없음"));

        PostLike postLike = postLikeRepository.findByUserIdAndPostId(me, postId);

        if(postLike == null){
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
            post.increaseLikeCount();
        } else{
            postLikeRepository.delete(postLike);
            post.decreaseLikeCount();
        }
    }
}

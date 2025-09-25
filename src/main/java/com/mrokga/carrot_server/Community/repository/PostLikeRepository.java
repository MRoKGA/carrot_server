package com.mrokga.carrot_server.Community.repository;

import com.mrokga.carrot_server.Community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {
    /**
     * 특정 사용자(userId)가 특정 게시글(postId)에 좋아요를 눌렀는지 확인
     * - 게시글 상세 조회 시 "likedByMe" 값 계산
     * - 좋아요 토글 API (/api/community/{postId}/like) 실행 시 중복 여부 확인
     */
    PostLike findByUserIdAndPostId(Integer userId, Integer postId);
    List<PostLike> findAllByPostId(Integer postId);
}

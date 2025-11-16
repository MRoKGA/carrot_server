package com.mrokga.carrot_server.community.repository;

import com.mrokga.carrot_server.community.entity.Comment;
import com.mrokga.carrot_server.community.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Integer> {
    /**
     * 특정 사용자(userId)가 특정 댓글(commentId)에 좋아요를 눌렀는지 확인
     * - 댓글 조회 시 "likedByMe" 값 계산
     * - 댓글 좋아요 토글 API (/api/community/comment/{commentId}/like) 실행 시 중복 여부 확인
     */
    CommentLike findByUserIdAndCommentId(Integer userId, Integer commentId);
    List<CommentLike> findAllByCommentId(Integer commentId);

    // 댓글 좋아요 전부 한 방에 삭제
    void deleteByCommentIn(List<Comment> comments);

    // 내가 좋아요 누른 댓글 ID 목록 한 번에 가져오기
    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<Integer> findLikedCommentIds(@Param("userId") Integer userId, @Param("commentIds") List<Integer> commentIds);
}

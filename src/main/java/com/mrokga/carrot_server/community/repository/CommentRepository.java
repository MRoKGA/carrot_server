package com.mrokga.carrot_server.community.repository;

import com.mrokga.carrot_server.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    /**
     * 특정 게시글(postId)에 달린 댓글들을 작성일(createdAt ASC) 순으로 조회
     * - 게시글 상세 조회 API (/api/community/{id}) 에서 댓글 리스트 출력할 때 사용
     */
    List<Comment> findByPostIdOrderByCreatedAtAsc(Integer postId);
}

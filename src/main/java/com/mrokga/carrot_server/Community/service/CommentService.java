package com.mrokga.carrot_server.Community.service;

import com.mrokga.carrot_server.Community.dto.request.CreateCommentRequestDto;
import com.mrokga.carrot_server.Community.dto.request.EditCommentRequestDto;
import com.mrokga.carrot_server.Community.entity.Comment;
import com.mrokga.carrot_server.Community.entity.CommentLike;
import com.mrokga.carrot_server.Community.entity.Post;
import com.mrokga.carrot_server.Community.entity.PostLike;
import com.mrokga.carrot_server.Community.repository.CommentLikeRepository;
import com.mrokga.carrot_server.Community.repository.CommentRepository;
import com.mrokga.carrot_server.Community.repository.PostRepository;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    // 댓글 작성
    public void createComment(CreateCommentRequestDto dto){
        Post post  = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("CommentService.createComment(): 게시글 없음"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("CommentService.createComment(): 유저 없음"));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(dto.getContent())
                .build();

        commentRepository.save(comment);
        post.increaseCommentCount();
    }

    // 댓글 수정
    public void editComment(EditCommentRequestDto dto, Integer me){
        Post post  = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("CommentService.editComment(): 게시글 없음"));

        Comment comment = commentRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("CommentService.editComment(): 댓글 없음"));

        if(!comment.getUser().getId().equals(me)){
            throw new SecurityException("CommentService.editComment(): 수정 권한 없음");
        }

        comment.setContent(dto.getContent());
    }

    // 댓글 삭제
    public void deleteComment(Integer commentId, Integer me){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("CommentService.deleteComment(): 댓글 없음"));

        if(!comment.getUser().getId().equals(me)){
            throw new SecurityException("CommentService.deleteComment(): 삭제 권한 없음");
        }

        Post post = comment.getPost();
        post.decreaseCommentCount();

        commentLikeRepository.deleteAll(commentLikeRepository.findAllByCommentId(commentId));
        commentRepository.delete(comment);
    }

    // 댓글 좋아요 토글
    public void toggleCommentLike(Integer me, Integer commentId){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("PostService.toggleCommentLike(): 댓글 없음"));

        User user = userRepository.findById(me)
                .orElseThrow(() -> new EntityNotFoundException("PostService.toggleCommentLike(): 유저 없음"));

        CommentLike like = commentLikeRepository.findByUserIdAndCommentId(me, commentId);

        if(like == null){
            commentLikeRepository.save(CommentLike.builder().comment(comment).user(user).build());
            comment.increaseLikeCount();
        } else{
            commentLikeRepository.delete(like);
            comment.decreaseLikeCount();
        }
    }
}

package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    @EntityGraph(
            attributePaths = {"parentMessage", "parentMessage.user"},
            type = EntityGraph.EntityGraphType.FETCH // 권장: 명시
    ) // N+1 제거/예외 방지/성능 안정화로 과정이 훨씬 깔끔
    List<ChatMessage> findByChatRoom_IdOrderByCreatedAtAscIdAsc(Integer chatRoomId);

    // 내가 보낸 마지막 메시지 (id 내림차순 Top 1)
    Optional<ChatMessage> findTopByChatRoom_IdAndUser_IdOrderByIdDesc(Integer roomId, Integer userId);

    // 방의 마지막 메시지 (목록 썸네일/미리보기용)
    Optional<ChatMessage> findTopByChatRoom_IdOrderByIdDesc(Integer roomId);
}

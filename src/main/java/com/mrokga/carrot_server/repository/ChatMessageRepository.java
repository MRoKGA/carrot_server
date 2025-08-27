package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    @EntityGraph(
            attributePaths = {"parent", "parent.user"},
            type = EntityGraph.EntityGraphType.FETCH // 권장: 명시
    ) // N+1 제거/예외 방지/성능 안정화로 과정이 훨씬 깔끔
    List<ChatMessage> findByChatRoom_IdOrderByCreatedAtAsc(Integer chatRoomId);
}

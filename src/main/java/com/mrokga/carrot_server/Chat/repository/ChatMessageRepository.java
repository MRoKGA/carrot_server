package com.mrokga.carrot_server.Chat.repository;

import com.mrokga.carrot_server.Chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // ====================== 커트라인(삭제 이후만) 전용 메소드 ======================
    /**
     * 방의 가장 큰 메세지 id = 삭제 시 커트라인 설정용
     * (메세지가 없다면 Optional.empty())
     */
    @Query("""
            select max(m.id)
            from ChatMessage m
            where m.chatRoom.id = :roomId
            """)
    Optional<Integer> findTopIdByRoomId(@Param("roomId") Integer roomId);

    /**
     * 내 커트라인 이후의 메시지들만 시간순으로 조회.
     * - 채팅방 입장 시 UI에 노출할 실제 메세지 목록.
     * - parentMessage, parentMessage.user 까지 페치해서 N+1 방지.
     */
    @EntityGraph(
            attributePaths = {"parentMessage", "parentMessage.user"},
            type = EntityGraph.EntityGraphType.FETCH
    )
    @Query("""
            select m
            from ChatMessage m
            where m.chatRoom.id = :roomId
              and m.id > :cutoffId
            order by m.createdAt asc, m.id asc
            """)
    List<ChatMessage> findAfterCutoff(@Param("roomId") Integer roomId,
                                      @Param("cutoffId") Integer cutoffId);

    /**
     * 내 커트라인 이후 + 내가 읽은 지점 이후의 "상대방이 보낸" 메세지 수.
     * - 안 읽음 배지 계산용. thresholdId = max(myLastReadId, myCutoffId)
     */
    @Query("""
            select count(m)
            from ChatMessage m
            where m.chatRoom.id = :roomId
              and m.id > :thresholdId
              and m.user.id <> :me
            """)
    long countUnreadAfter(@Param("roomId") Integer roomId,
                             @Param("thresholdId") Integer thresholdId,
                             @Param("me") Integer me);

    /**
     * 내 커트라인 이후의 "마지막으로 보이는 메세지 id"
     * - 목록 미리보기/정렬 및 빠른 조회에 사용.
     */
    @Query("""
            select max(m.id)
            from ChatMessage m
            where m.chatRoom.id = :roomId
              and m.id > :cutoffId
            """)
    Optional<Integer> findLastVisibleMsgId(@Param("roomId") Integer roomId,
                                            @Param("cutoffId") Integer cutoffId);

    /**
     * 내 커트라인 이후 메시지가 1개라도 존재하는지 빠른 체크.
     * - 목록 노출 여부 / 알림 여부 판정용
     */
    @Query("""
            select (count(m) > 0)
            from ChatMessage m
            where m.chatRoom.id = :roomId
              and m.id > :cutoffId
            """)
    boolean existsAfterCutoff(@Param("roomId") Integer roomId,
                              @Param("cutoffId") Integer cutoffId);

    /**
     * 내 커트라인 "이후의 첫번째 메세지" 조회.
     */
    @Query("""
            select m
            from ChatMessage m
            where m.chatRoom.id = :roomId
              and m.id > :cutoffId
            order by m.id asc
            """)
    Optional<ChatMessage> findFirstAfterCutoff(@Param("roomId") Integer roomId,
                                               @Param("cutoffId") Integer cutoffId);
}

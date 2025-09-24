package com.mrokga.carrot_server.Chat.repository;

import com.mrokga.carrot_server.Chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    // 동일 상품/구매자/판매자 조합의 채팅방 단건 조회.
    Optional<ChatRoom> findByProduct_IdAndBuyer_IdAndSeller_Id(Integer productId, Integer buyerId, Integer sellerId);

    // buyer 또는 seller로 참여 중인 모든 채팅방을 단순 조회.
    List<ChatRoom> findByBuyer_IdOrSeller_Id(Integer buyerId, Integer sellerId);

    /* ===========================
     * 읽음 포인터 관련
     * =========================== */
    // buyer의 lastReadMessageId를 안전하게 올려준다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update ChatRoom r
         set r.buyerLastReadMessageId =
           CASE WHEN r.buyerLastReadMessageId is null or r.buyerLastReadMessageId < :messageId
                THEN :messageId ELSE r.buyerLastReadMessageId END
       where r.id = :roomId
    """)
    int bumpBuyerLastRead(Integer roomId, Integer messageId);

    // seller의 lastReadMessageId를 안전하게 올려준다
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update ChatRoom r
         set r.sellerLastReadMessageId =
           CASE WHEN r.sellerLastReadMessageId is null or r.sellerLastReadMessageId < :messageId
                THEN :messageId ELSE r.sellerLastReadMessageId END
       where r.id = :roomId
    """)
    int bumpSellerLastRead(Integer roomId, Integer messageId);


    /* ===========================
     * 삭제(=커트라인) 관련
     * - cutoffId: 이 시점의 마지막 메시지 id (없으면 0을 주는 설계 권장)
     * - 이후 사용자에게 id <= cutoffId 인 메시지는 영원히 보이지 않음(복구 불가).
     * - 감사/신고 대비를 위해 DB엔 메시지 자체는 남김.
     * =========================== */

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update ChatRoom r
         set r.buyerDeleteCutoffMessageId = :cutoffId,
             r.buyerDeletedAt = CURRENT_TIMESTAMP
       where r.id = :roomId and r.buyer.id = :buyerId
    """)
    int markBuyerDeleted(@Param("roomId") Integer roomId,
                         @Param("buyerId") Integer buyerId,
                         @Param("cutoffId") Integer cutoffId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update ChatRoom r
         set r.sellerDeleteCutoffMessageId = :cutoffId,
             r.sellerDeletedAt = CURRENT_TIMESTAMP
       where r.id = :roomId and r.seller.id = :sellerId
    """)
    int markSellerDeleted(@Param("roomId") Integer roomId,
                          @Param("sellerId") Integer sellerId,
                          @Param("cutoffId") Integer cutoffId);



    /* ===========================
     * 가시성(목록/입장 전용) 조회
     * 내가 "볼 수 있는" 채팅방만 조회(목록용).
     * - 기준: 내 커트라인 이후(id > cutoff)의 메시지가 최소 1개 이상 존재하는 방만 리턴.
     * - 즉, 내가 방을 삭제한 뒤 아직 새 메시지가 오지 않았다면 목록에서 제외됨.
     * - 새 메시지가 오면 다시 목록에 뜨되, 이전 기록은 잘린 채로 보이게 됨.
     * =========================== */
    @Query("""
    select r
      from ChatRoom r
     where (r.buyer.id = :uid or r.seller.id = :uid)
       and exists (
            select 1 from ChatMessage m
             where m.chatRoom.id = r.id
               and m.id >
                   case when r.buyer.id = :uid
                        then coalesce(r.buyerDeleteCutoffMessageId, 0)
                        else coalesce(r.sellerDeleteCutoffMessageId, 0)
                   end
       )
     order by r.id desc
    """)
    List<ChatRoom> findVisibleRoomsForUser(@Param("uid") Integer uid);

    /**
     * 특정 방이 "나에게 보이는가?"를 빠르게 체크.
     * - 입장 전 권한/가시성 가드에 사용하면 좋음.
     */
    @Query("""
    select (count(r) > 0)
      from ChatRoom r
     where r.id = :roomId
       and (r.buyer.id = :uid or r.seller.id = :uid)
       and exists (
            select 1 from ChatMessage m
             where m.chatRoom.id = r.id
               and m.id >
                   case when r.buyer.id = :uid
                        then coalesce(r.buyerDeleteCutoffMessageId, 0)
                        else coalesce(r.sellerDeleteCutoffMessageId, 0)
                   end
       )
    """)
    boolean existsVisibleForUser(@Param("roomId") Integer roomId, @Param("uid") Integer uid);

    /**
     * 내 관점에서 적용해야 할 "삭제 커트라인 id"를 반환.
     * - 서비스 계층에서 메시지 리스트/안읽음 계산 시 공통으로 사용.
     * - buyer면 buyer 커트라인, seller면 seller 커트라인을 돌려줌(null이면 0 취급 권장).
     */
    @Query("""
    select case
         when r.buyer.id  = :uid then coalesce(r.buyerDeleteCutoffMessageId, 0)
         when r.seller.id = :uid then coalesce(r.sellerDeleteCutoffMessageId, 0)
        end
    from ChatRoom r
    where r.id = :roomId
        and (r.buyer.id = :uid or r.seller.id = :uid)
    """)
    Integer getDeleteCutoffForUser(@Param("roomId") Integer roomId, @Param("uid") Integer uid);

    /**
     * 내 관점에서 "마지막으로 보이는 메시지 id"를 반환.
     * - 미리보기/정렬에 사용 가능(가시성 기준으로 마지막 메시지 구하기).
     * - 메시지 없으면 null.
     */
    @Query("""
    select max(m.id)
      from ChatRoom r
      join ChatMessage m on m.chatRoom.id = r.id
     where r.id = :roomId
       and (r.buyer.id = :uid or r.seller.id = :uid)
       and m.id >
            case when r.buyer.id = :uid
                 then coalesce(r.buyerDeleteCutoffMessageId, 0)
                 else coalesce(r.sellerDeleteCutoffMessageId, 0)
            end
    """)
    Optional<Integer> findLastVisibleMessageId(@Param("roomId") Integer roomId, @Param("uid") Integer uid);

    /**
     * 내 관점에서 "새 메시지가 있는 방(안읽음 유무와는 별개로, 삭제 이후의 새 활동 존재)"만 카운트.
     * - 알림 배지 숫자, "대화가 다시 시작된 방 수" 같은 지표에 활용 가능.
     */
    @Query("""
    select count(r)
      from ChatRoom r
     where (r.buyer.id = :uid or r.seller.id = :uid)
       and exists (
            select 1 from ChatMessage m
             where m.chatRoom.id = r.id
               and m.id >
                   case when r.buyer.id = :uid
                        then coalesce(r.buyerDeleteCutoffMessageId, 0)
                        else coalesce(r.sellerDeleteCutoffMessageId, 0)
                   end
       )
    """)
    long countRoomsVisibleForUser(@Param("uid") Integer uid);
}

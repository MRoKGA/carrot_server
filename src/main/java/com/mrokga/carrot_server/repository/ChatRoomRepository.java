package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    Optional<ChatRoom> findByProduct_IdAndBuyer_IdAndSeller_Id(Integer productId, Integer buyerId, Integer sellerId);
    List<ChatRoom> findByBuyer_IdOrSeller_Id(Integer buyerId, Integer sellerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update ChatRoom r
         set r.buyerLastReadMessageId =
           CASE WHEN r.buyerLastReadMessageId is null or r.buyerLastReadMessageId < :messageId
                THEN :messageId ELSE r.buyerLastReadMessageId END
       where r.id = :roomId
    """)
    int bumpBuyerLastRead(Integer roomId, Integer messageId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update ChatRoom r
         set r.sellerLastReadMessageId =
           CASE WHEN r.sellerLastReadMessageId is null or r.sellerLastReadMessageId < :messageId
                THEN :messageId ELSE r.sellerLastReadMessageId END
       where r.id = :roomId
    """)
    int bumpSellerLastRead(Integer roomId, Integer messageId);
}

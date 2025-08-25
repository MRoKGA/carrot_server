package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    Optional<ChatRoom> findByProductIdAndBuyerIdAndSellerId(Integer productId, Integer buyerId, Integer sellerId);
    List<ChatRoom> findByBuyerId(Integer buyerId);
    List<ChatRoom> findBySellerId(Integer sellerId);
}

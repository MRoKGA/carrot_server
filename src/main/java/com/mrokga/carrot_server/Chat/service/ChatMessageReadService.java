package com.mrokga.carrot_server.Chat.service;

import com.mrokga.carrot_server.Chat.entity.ChatMessage;
import com.mrokga.carrot_server.Chat.entity.ChatRoom;
import com.mrokga.carrot_server.Chat.repository.ChatMessageRepository;
import com.mrokga.carrot_server.Chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatMessageReadService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsRead(Integer roomId, Integer messageId, Integer userId) {
        // 권한/검증
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지 없음"));
        if (!Objects.equals(msg.getChatRoom().getId(), roomId)) {
            throw new IllegalArgumentException("다른 방 메시지 읽음 처리 불가");
        }

        boolean isBuyer = Objects.equals(userId, room.getBuyer().getId());
        boolean isSeller = Objects.equals(userId, room.getSeller().getId());
        if (!isBuyer && !isSeller) throw new AccessDeniedException("읽음 처리 권한 없음");

        if (isBuyer) {
            chatRoomRepository.bumpBuyerLastRead(roomId, messageId);
        } else { // seller
            chatRoomRepository.bumpSellerLastRead(roomId, messageId);
        }
    }
}

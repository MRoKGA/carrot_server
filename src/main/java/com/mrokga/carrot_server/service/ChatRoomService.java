package com.mrokga.carrot_server.service;

import com.mrokga.carrot_server.dto.request.ChatRoomRequestDto;
import com.mrokga.carrot_server.dto.response.ChatRoomResponseDto;
import com.mrokga.carrot_server.entity.ChatMessage;
import com.mrokga.carrot_server.entity.ChatRoom;
import com.mrokga.carrot_server.entity.Product;
import com.mrokga.carrot_server.entity.User;
import com.mrokga.carrot_server.enums.MessageType;
import com.mrokga.carrot_server.repository.ChatMessageRepository;
import com.mrokga.carrot_server.repository.ChatRoomRepository;
import com.mrokga.carrot_server.repository.ProductRepository;
import com.mrokga.carrot_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private ChatRoomResponseDto toResponse(ChatRoom room) {
        ChatRoomResponseDto dto = new ChatRoomResponseDto();
        dto.setRoomId(room.getId());
        dto.setProductId(room.getProduct().getId());
        dto.setBuyerId(room.getBuyer().getId());
        dto.setSellerId(room.getSeller().getId());
        dto.setCreatedAt(room.getCreatedAt());

        return dto;
    }

    @Transactional
    public ChatRoomResponseDto createOrGetRoom(Integer me, ChatRoomRequestDto dto){
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        Integer ownerId = product.getUser().getId(); // 판매자 확정(dto sellerId 무시)

        final User seller;
        final User buyer;

        // 판매자 선톡
        if(Objects.equals(me, ownerId)){
            if(dto.getBuyerId() == null){
                throw new IllegalArgumentException("구매자 ID가 필요합니다.(판매자 선톡)");
            }
            seller = userRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("판매자 없음"));
            buyer = userRepository.findById(dto.getBuyerId())
                    .orElseThrow(() -> new IllegalArgumentException("구매자 없음"));
        }
        // 구매자 선톡
        else{
            buyer = userRepository.findById(me)
                    .orElseThrow(() -> new IllegalArgumentException("구매자 없음"));
            seller = userRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("판매자 없음"));

            // 클라이언트가 sellerId/buyerId를 보내왔더라도 신뢰하지 않음
            // 보냈다면 검증만: 불일치 시 오류
            if (dto.getSellerId() != null && !Objects.equals(dto.getSellerId(), ownerId)) {
                throw new AccessDeniedException("sellerId 위조 의심: product 소유자와 불일치");
            }
            if (dto.getBuyerId() != null && !Objects.equals(dto.getBuyerId(), me)) {
                throw new AccessDeniedException("buyerId 위조 의심: 현재 사용자와 불일치");
            }
        }

        if(Objects.equals(seller.getId(), buyer.getId())){
            throw new IllegalArgumentException("판매자와 구매자가 동일할 수 없습니다.");
        }

        // 조회 또는 생성
        try {
            ChatRoom chatRoom = chatRoomRepository.findByProduct_IdAndBuyer_IdAndSeller_Id(
                            product.getId(), buyer.getId(), seller.getId())
                    .orElseGet(() -> chatRoomRepository.save(
                            ChatRoom.builder().product(product).buyer(buyer).seller(seller).build()
                    ));

            return toResponse(chatRoom);
        }catch(DataIntegrityViolationException e){ // DB에 유니크 제약 걸어뒀으나, 혹시 모를 동시성 충돌 시 재조회
            return chatRoomRepository.findByProduct_IdAndBuyer_IdAndSeller_Id(
                    product.getId(), buyer.getId(), seller.getId())
                    .map(this::toResponse)
                    .orElseThrow(() -> e);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto> getRoomByUser(Integer userId){
        List<ChatRoom> rooms = chatRoomRepository.findByBuyer_IdOrSeller_Id(userId, userId);
        return rooms.stream().map(room -> {
            ChatRoomResponseDto dto = toResponse(room); // 기존 필드 세팅

            // 방의 마지막 메시지 (미리보기용)
            chatMessageRepository.findTopByChatRoom_IdOrderByIdDesc(room.getId())
                    .ifPresent(last -> {
                        dto.setLastMessageId(last.getId());
                        dto.setLastMessageSenderId(last.getUser().getId());
                        dto.setLastMessageAt(last.getCreatedAt());
                        dto.setLastMessagePreview(
                                last.getMessageType() == MessageType.TEXT
                                        ? abbreviate(last.getMessage(), 50)
                                        : "[이미지]"
                        );
                    });

            // 내가 보낸 마지막 메시지 ID
            Integer lastMyMessageId = chatMessageRepository
                    .findTopByChatRoom_IdAndUser_IdOrderByIdDesc(room.getId(), userId)
                    .map(ChatMessage::getId)
                    .orElse(null);

            // 상대의 lastRead 포인터
            Integer opponentRead = Objects.equals(userId, room.getBuyer().getId())
                    ? room.getSellerLastReadMessageId()
                    : room.getBuyerLastReadMessageId();

            boolean seen = (lastMyMessageId != null) && (opponentRead != null) && (opponentRead >= lastMyMessageId);
            dto.setLastMessageSeen(seen);

            return dto;
        }).toList();
    }

    @Transactional
    public void deleteRoom(Integer roomId, Integer userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        Integer buyerId = chatRoom.getBuyer().getId();
        Integer sellerId = chatRoom.getSeller().getId();

        boolean isMember = Objects.equals(userId, buyerId) || Objects.equals(userId, sellerId);

        if(!isMember){
            throw new AccessDeniedException("채팅방 삭제 권한이 없습니다.");
        }

        chatRoomRepository.delete(chatRoom);
    }

    private String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}

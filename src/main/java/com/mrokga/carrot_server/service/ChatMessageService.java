package com.mrokga.carrot_server.service;

import com.mrokga.carrot_server.dto.request.MessageRequestDto;
import com.mrokga.carrot_server.dto.response.MessageResponseDto;
import com.mrokga.carrot_server.entity.ChatMessage;
import com.mrokga.carrot_server.entity.ChatRoom;
import com.mrokga.carrot_server.entity.User;
import com.mrokga.carrot_server.enums.MessageType;
import com.mrokga.carrot_server.repository.ChatMessageRepository;
import com.mrokga.carrot_server.repository.ChatRoomRepository;
import com.mrokga.carrot_server.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReadService chatMessageReadService;
    private final SimpMessageSendingOperations messagingTemplate; // ✅ 실시간 전송 기능 추가

    // 메세지 객체 DTO 형태로 변환 메소드
    private MessageResponseDto toResponse(ChatMessage message){
        MessageResponseDto dto = new MessageResponseDto();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSenderId(message.getUser().getId());
        dto.setMessageType(message.getMessageType().name());
        dto.setMessage(message.getMessage());
        dto.setCreatedAt(message.getCreatedAt());

        // 부모(답장 대상) 있으면 요약 채우기
        ChatMessage p = message.getParentMessage();
        if (p != null) {
            dto.setReplyToMessageId(p.getId());
            dto.setReplyToSenderId(p.getUser().getId());
            dto.setReplyToPreview(
                    p.getMessageType() == MessageType.TEXT
                            ? abbreviate(p.getMessage(), 50)
                            : "[이미지]"
            );
        }

        return dto;
    }

    // 답장 메세지, 부모 메세지 미리보기 50자
    private String abbreviate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    // ✅ 기존 sendMessage: REST API용 (메시지 저장만 수행)
    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto dto, Integer senderId){
        return saveMessage(dto, senderId);
    }

    // ✅ 새로운 sendMessageAndBroadcast: WebSocket 전용 (저장 + 실시간 전송)
    @Transactional
    public void sendMessageAndBroadcast(MessageRequestDto dto, Integer senderId){
        MessageResponseDto response = saveMessage(dto, senderId);

        // 해당 채팅방 구독자에게 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/room/" + dto.getChatRoomId(), response);
    }

    //메세지 전송
    @Transactional
    public MessageResponseDto saveMessage(MessageRequestDto dto, Integer senderId){
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        ChatRoom room = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        //전송 권한 확인
        Integer sellerId = room.getSeller().getId();
        Integer buyerId = room.getBuyer().getId();
        boolean isMember = Objects.equals(senderId, sellerId) || Objects.equals(senderId,buyerId);
        if(!isMember){
            throw new AccessDeniedException("채팅방 메세지 전송 권한이 없습니다.");
        }

        // 프론트에서 넘겨준 messageType 없으면 TEXT 기본값
        MessageType type = dto.getMessageType() != null ? dto.getMessageType() : MessageType.TEXT;

        // 타입별 최소 검증(IMAGE는 URL이어야 등)
        if (type == MessageType.IMAGE && (dto.getMessage() == null || !dto.getMessage().startsWith("http"))) {
            throw new IllegalArgumentException("IMAGE 타입은 이미지 URL이어야 합니다.");
        }
        if (type == MessageType.TEXT && (dto.getMessage() == null || dto.getMessage().isBlank())) {
            throw new IllegalArgumentException("TEXT 메시지는 비어 있을 수 없습니다.");
        }

        // 답장 대상 로드 & 같은 방인지 검증
        ChatMessage parent = null;
        if (dto.getReplyToMessageId() != null) {
            parent = chatMessageRepository.findById(dto.getReplyToMessageId())
                    .orElseThrow(() -> new IllegalArgumentException("답장 대상 메시지 없음"));
            if (!parent.getChatRoom().getId().equals(room.getId())) {
                throw new IllegalArgumentException("다른 채팅방의 메시지에는 답장할 수 없습니다.");
            }
            // (선택) parent가 삭제되었으면 금지할지 허용할지 정책 결정
        }

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.builder()
                        .user(sender)
                        .chatRoom(room)
                        .message(dto.getMessage())
                        .messageType(type)
                        .parentMessage(parent)
                        .build()
        );

        return toResponse(message);
    }

    // 메세지 조회
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessages(Integer roomId, Integer requesterId){
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        // 조회 권한 확인
        Integer sellerId = room.getSeller().getId();
        Integer buyerId = room.getBuyer().getId();
        boolean isMember = Objects.equals(requesterId, buyerId) || Objects.equals(requesterId, sellerId);
        if(!isMember){
            throw new AccessDeniedException("채팅방 메세지 조회 권한이 없습니다.");
        }

        // 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatRoom_IdOrderByCreatedAtAscIdAsc(roomId);

        // ===== 여기서 읽음 처리 =====
        if (!messages.isEmpty()) {
            int lastMessageId = messages.get(messages.size() - 1).getId();
            // 이거 markAsRead 서비스 호출
            chatMessageReadService.markAsRead(room.getId(), lastMessageId, requesterId);
        }

        // ====== 마지막 메시지 '읽음' 판정 ======
        Integer opponentReadId = Objects.equals(requesterId, buyerId)
                ? room.getSellerLastReadMessageId() : room.getBuyerLastReadMessageId();

        ChatMessage lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1);
        Integer lastMsgId = (lastMsg != null) ? lastMsg.getId() : null;
        boolean lastIsMine = (lastMsg != null) && Objects.equals(lastMsg.getUser().getId(), requesterId);

        // 마지막 메시지가 내가 보낸 거라면, 상대 포인터가 그 ID 이상인지로 판정
        boolean lastMsgReadByOpponent = lastIsMine && opponentReadId != null
                                         && lastMsgId != null && opponentReadId >= lastMsgId;

        // ====== DTO 매핑 + 플래그 세팅 ======
        final Integer lastIdFinal = lastMsgId;
        final boolean lastReadFinal = lastMsgReadByOpponent;

        return messages.stream().map(m -> {
            MessageResponseDto dto = toResponse(m);

            // 프론트 버블 정렬/색 구분에 유용
            boolean mine = Objects.equals(m.getUser().getId(), requesterId);
            dto.setMine(mine);

            // "마지막 메시지"이면서 내가 보낸 메시지일 때만 읽음 플래그 세팅
            dto.setReadByOpponent(Objects.equals(lastIdFinal, m.getId()) && lastReadFinal);

            return dto;
        }).toList();

    }


}

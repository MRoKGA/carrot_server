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

    //메세지 전송
    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto dto, Integer senderId){
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
        return chatMessageRepository.findByChatRoom_IdOrderByCreatedAtAsc(roomId)
                .stream().map(this::toResponse).toList();
    }


}

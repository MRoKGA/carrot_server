package com.mrokga.carrot_server.controller;

import com.mrokga.carrot_server.dto.request.MessageRequestDto;
import com.mrokga.carrot_server.dto.response.MessageResponseDto;
import com.mrokga.carrot_server.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "ChatMessage", description = "메세지 전송, 조회 API")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    private Integer getCurrentUserId(){
        return Integer.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Operation(summary = "메세지 전송", description = "사용자가 메세지를 전송합니다.")
    @PostMapping("/{roomId}/message")
    public MessageResponseDto sendMessage(@PathVariable Integer roomId, @RequestBody MessageRequestDto dto) {
        if(dto.getChatRoomId() != null && !dto.getChatRoomId().equals(roomId)){
            throw new IllegalArgumentException("path의 roomId와 dto의 roomId 불일치");
        }
        dto.setChatRoomId(roomId);
        return chatMessageService.sendMessage(dto, getCurrentUserId());
    }

    @Operation(summary = "채팅방 메세지 조회", description = "사용자가 특정 채팅방의 메세지들을 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public List<MessageResponseDto> getMessages(@PathVariable Integer roomId) {
        return chatMessageService.getMessages(roomId, getCurrentUserId());
    }
}

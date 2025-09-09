package com.mrokga.carrot_server.controller;

import com.mrokga.carrot_server.dto.request.ChatRoomRequestDto;
import com.mrokga.carrot_server.dto.response.ChatRoomResponseDto;
import com.mrokga.carrot_server.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatRoom")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 생성,조회,삭제 API")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    private Integer getCurrentUserId(){
        return Integer.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Operation(summary = "채팅방 생성 및 불러오기", description = "사용자가 처음 채팅을 건 경우 채팅방을 생성, 이미 채팅 내역이 있다면 채팅방을 불러옵니다.")
    @PostMapping
    public ChatRoomResponseDto createOrGetRoom(@RequestBody @Valid ChatRoomRequestDto dto){
        return chatRoomService.createOrGetRoom(getCurrentUserId(), dto);
    }

    @Operation(summary = "채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    @GetMapping
    public List<ChatRoomResponseDto> myRooms() {
        return chatRoomService.getRoomByUser(getCurrentUserId());
    }

    @Operation(summary = "채팅방 삭제", description = "사용자가 채팅방을 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable Integer roomId) {
        chatRoomService.deleteRoom(roomId, getCurrentUserId());
    }
}

package com.mrokga.carrot_server.Chat.controller;

import com.mrokga.carrot_server.Chat.dto.request.ChatRoomRequestDto;
import com.mrokga.carrot_server.Chat.dto.response.ChatRoomResponseDto;
import com.mrokga.carrot_server.Chat.service.ChatRoomService;
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

    private Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."
            );
        }
        // TokenProvider.getAuthentication()에서 subject를 userId로 넣어두셨으므로 getName() = userId
        return Integer.valueOf(auth.getName());
    }

    @Operation(
            summary = "채팅방 생성/불러오기",
            description = """
        - 사용자가 처음 채팅을 시작하면 채팅방을 생성합니다.
        - 이미 채팅방이 존재하면 기존 방을 반환합니다.
        - 구매자/판매자 ID는 서버에서 검증되며, 클라이언트가 임의 조작할 수 없습니다.
        """
    )
    @PostMapping
    public ChatRoomResponseDto createOrGetRoom(@RequestBody @Valid ChatRoomRequestDto dto){
        return chatRoomService.createOrGetRoom(getCurrentUserId(), dto);
    }

    @Operation(
            summary = "채팅방 목록 조회",
            description = """
        - 사용자가 참여 중인 채팅방 목록을 조회합니다.
        - 단, 내가 삭제한 이후(cutoff) 새 메시지가 없는 방은 목록에 표시되지 않습니다.
        - 목록에는 마지막 메시지 요약, 읽음 여부 등이 포함됩니다.
        """
    )
    @GetMapping
    public List<ChatRoomResponseDto> myRooms() {
        return chatRoomService.getRoomByUser(getCurrentUserId());
    }

    @Operation(
            summary = "채팅방 삭제 (소프트 삭제)",
            description = """
        - 사용자가 채팅방을 삭제합니다.
        - 실제 DB에서 방과 메시지가 삭제되지 않고, 내 쪽에서만 '숨김' 처리됩니다.
        - 이후 새 메시지가 도착하면 이 방은 다시 목록에 나타나며, 과거 메시지는 보이지 않습니다.
        """
    )
    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable Integer roomId) {
        chatRoomService.deleteRoom(roomId, getCurrentUserId());
    }
}

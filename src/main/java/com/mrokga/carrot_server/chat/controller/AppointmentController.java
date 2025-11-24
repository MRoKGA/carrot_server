package com.mrokga.carrot_server.chat.controller;

import com.mrokga.carrot_server.chat.dto.request.AppointmentRequestDto;
import com.mrokga.carrot_server.chat.dto.response.AppointmentResponseDto;
import com.mrokga.carrot_server.chat.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat/{roomId}/appointment")
@RequiredArgsConstructor
@Tag(name = "Appointment API", description = "채팅 약속(거래 예약) API")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Operation(summary = "약속 제안", description = "채팅방에서 구매자/판매자 중 누구든 약속을 제안합니다.(시스템 메시지: %s님이 %s %s에 만나자고 약속을 제안했습니다.)")
    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(
            @PathVariable Integer roomId,
            @RequestBody @Valid AppointmentRequestDto req) {

        return ResponseEntity.ok(appointmentService.create(roomId, req));
    }

    @Operation(summary = "약속 수락", description = "상대방이 제안한 약속을 수락하면 상품 상태가 RESERVED로 변경됩니다.(시스템 메시지: 약속이 수락되었습니다. 상품 상태가 예약중으로 변경됩니다.)")
    @PatchMapping("/{appointmentId}/accept")
    public ResponseEntity<AppointmentResponseDto> accept(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok(appointmentService.acceptAppointment(appointmentId));
    }

    @Operation(summary = "약속 거절", description = "상대방이 제안한 약속을 거절합니다. 싱품 상태는 그대로 유지됩니다.(시스템 메시지: 약속이 거절되었습니다.)")
    @PatchMapping("/{appointmentId}/reject")
    public ResponseEntity<AppointmentResponseDto> reject(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok(appointmentService.rejectAppointment(appointmentId));
    }

    @Operation(summary = "약속 취소", description = "이미 생성된 약속을 취소합니다. 상품 상태는 ON_SALE로 되돌아갑니다.(시스템 메시지: 약속이 취소되었습니다. 상품 상태가 판매중으로 돌아갑니다.)")
    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
    }

    @Operation(summary = "약속 조회", description = "채팅방 ID로 해당 약속 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<AppointmentResponseDto> getAppointment(@PathVariable Integer roomId) {
        return ResponseEntity.ok(appointmentService.getAppointmentByChatRoomId(roomId));
    }
}

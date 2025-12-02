package com.mrokga.carrot_server.chat.service;

import com.mrokga.carrot_server.chat.entity.ChatRoom;
import com.mrokga.carrot_server.chat.repository.ChatRoomRepository;
import com.mrokga.carrot_server.chat.dto.request.AppointmentRequestDto;
import com.mrokga.carrot_server.chat.dto.response.AppointmentResponseDto;
import com.mrokga.carrot_server.chat.entity.Appointment;
import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import com.mrokga.carrot_server.chat.repository.AppointmentRepository;
import com.mrokga.carrot_server.product.dto.request.ChangeStatusRequestDto;
import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.enums.TradeStatus;
import com.mrokga.carrot_server.product.service.ProductService;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 채팅방 내에서 이뤄지는 기능
// 채팅방 조회 및 열람에서 본인 인증을 통해 채팅방 참여자 여부를 가려놨기에
// 여기서는 따로 채팅 참여자 여부를 가리지 않음. (즉 약속 CRUD 권한 여부 가리지 않음. 아미 조회 및 열람했으면 약속에 대한 권한도 당연히 부여)
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ChatMessageService chatMessageService;

    // 엔티티 → DTO 변환 메서드
    private AppointmentResponseDto toDto(Appointment appointment) {
        return AppointmentResponseDto.builder()
                .id(appointment.getId())
                .chatRoomId(appointment.getChatRoom().getId())
                .proposerId(appointment.getProposer().getId())
                .meetingTime(appointment.getMeetingTime())
                .meetingPlace(appointment.getMeetingPlace())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .build();
    }

    // 약속 생성
    @Transactional
    public AppointmentResponseDto create(Integer roomId, AppointmentRequestDto dto){
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("AppointmentService.create(): 채팅방 없음"));

        User proposer = userRepository.findById(dto.getProposerId())
                .orElseThrow(() -> new EntityNotFoundException("AppointmentService.create(): 유저 없음"));

        // 중복 약속 방지: 해당 채팅방에 PENDING/ACCEPTED 상태 약속이 있으면 생성 불가
        appointmentRepository.findByChatRoom_IdAndStatusIn(
                roomId, java.util.List.of(AppointmentStatus.PENDING, AppointmentStatus.ACCEPTED)
        ).ifPresent(a -> {
            throw new IllegalStateException("이미 진행 중인 약속이 있습니다. 취소/거절 후 다시 시도하세요.");
        });

        Appointment appointment = Appointment.builder()
                .chatRoom(room)
                .proposer(proposer)
                .meetingTime(dto.getMeetingTime())
                .meetingPlace(dto.getMeetingPlace())
                .status(AppointmentStatus.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // 시스템 메시지 생성 후 전송
        String content = String.format("%s님이 %s %s에 만나자고 약속을 제안했습니다.",
                proposer.getNickname(),
                dto.getMeetingTime().toLocalDate(),
                dto.getMeetingTime().toLocalTime());
        chatMessageService.sendSystemMessage(room, content);

        return toDto(saved);
    }

    // 약속 수락
    @Transactional
    public AppointmentResponseDto acceptAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("약속을 찾을 수 없습니다."));

        // 약속 상태 ACCEPTED 로 변경
        appointment.setStatus(AppointmentStatus.ACCEPTED);

        // 상품 상태 예약중으로 변경
        ChatRoom room = appointment.getChatRoom();
        Product product = room.getProduct();

        ChangeStatusRequestDto dto = ChangeStatusRequestDto.builder()
                .productId(product.getId())
                .sellerId(room.getSeller().getId())
                .status(TradeStatus.RESERVED)
                .buyerId(room.getBuyer().getId())
                .completedAt(null)
                .build();

        productService.changeStatus(dto);

        // 시스템 메시지 생성 후 전송
        String content = "약속이 수락되었습니다. 상품 상태가 예약중으로 변경됩니다.";
        chatMessageService.sendSystemMessage(room, content);

        return toDto(appointment);
    }

    // 약속 거절
    @Transactional
    public AppointmentResponseDto rejectAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("약속을 찾을 수 없습니다."));

        appointment.setStatus(AppointmentStatus.REJECTED);

        // 시스템 메시지 생성 후 전송
        String content = "약속이 거절되었습니다.";
        chatMessageService.sendSystemMessage(appointment.getChatRoom(), content);

        return toDto(appointment);
    }

    // 약속 취소
    @Transactional
    public AppointmentResponseDto cancelAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("약속을 찾을 수 없습니다."));

        appointment.setStatus(AppointmentStatus.CANCELED);

        ChatRoom room = appointment.getChatRoom();
        Product product = room.getProduct();

        // changeStatus 호출 (Transaction까지 정리)
        ChangeStatusRequestDto dto = ChangeStatusRequestDto.builder()
                .productId(product.getId())
                .sellerId(room.getSeller().getId())
                .status(TradeStatus.ON_SALE)  // 다시 판매중으로 되돌림
                .buyerId(room.getBuyer().getId()) // 이미 buyerId 있을 수도 있음, null도 허용
                .completedAt(null)
                .build();

        productService.changeStatus(dto);

        // 시스템 메시지 생성 후 전송
        String content = "약속이 취소되었습니다. 상품 상태가 판매중으로 돌아갑니다.";
        chatMessageService.sendSystemMessage(room, content);

        return toDto(appointment);
    }

    // 약속 조회
    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentByChatRoomId(Integer roomId) {
        Appointment appointment = appointmentRepository.findByChatRoom_Id(roomId)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방에 약속이 없습니다."));

        return AppointmentResponseDto.builder()
                .id(appointment.getId())
                .chatRoomId(appointment.getChatRoom().getId())
                .proposerId(appointment.getProposer().getId())
                .meetingTime(appointment.getMeetingTime())
                .meetingPlace(appointment.getMeetingPlace())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .build();
    }



}

package com.mrokga.carrot_server.chat.repository;

import com.mrokga.carrot_server.chat.entity.Appointment;
import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    // 특정 채팅방에 PENDING 또는 ACCEPTED 상태인 약속이 있는지 확인
    Optional<Appointment> findByChatRoom_IdAndStatusIn(Integer chatRoomId,
                                                       Iterable<AppointmentStatus> statuses);
}

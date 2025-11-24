package com.mrokga.carrot_server.chat.repository;

import com.mrokga.carrot_server.chat.entity.Appointment;
import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    // 특정 채팅방에 PENDING 또는 ACCEPTED 상태인 약속이 있는지 확인
    Optional<Appointment> findByChatRoom_IdAndStatusIn(Integer chatRoomId,
                                                       Iterable<AppointmentStatus> statuses);

    Optional<Appointment> findByChatRoom_Id(Integer chatRoomId);

    // ✔ 나의 약속(채팅방 참여자이거나 제안자=나). 상태 필터 optional
    @Query("""
    SELECT a
    FROM Appointment a
    JOIN a.chatRoom r
    WHERE (r.buyer.id = :userId OR r.seller.id = :userId OR a.proposer.id = :userId)
      AND (:status IS NULL OR a.status = :status)
    ORDER BY a.createdAt DESC
    """)
    Page<Appointment> findMyAppointments(Integer userId, com.mrokga.carrot_server.chat.enums.AppointmentStatus status, Pageable pageable);

}

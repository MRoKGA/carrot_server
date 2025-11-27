package com.mrokga.carrot_server.group.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupJoinRequestResponse {
    private Integer id;
    private Integer groupId;
    private Integer userId;
    private String  userNickname;
    private String  status;     // PENDING/APPROVED/REJECTED
    private String  message;
    private LocalDateTime createdAt;
}
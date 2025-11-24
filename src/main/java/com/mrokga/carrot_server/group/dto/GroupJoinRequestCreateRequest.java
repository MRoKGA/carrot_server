package com.mrokga.carrot_server.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(name = "GroupJoinRequestCreateRequest", description = "그룹 가입요청 메시지")
public class GroupJoinRequestCreateRequest {
    @Schema(description = "가입 메시지", example = "매주 참여 가능해요!")
    private String message;
}

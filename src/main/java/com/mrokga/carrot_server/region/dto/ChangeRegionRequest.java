package com.mrokga.carrot_server.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeRegionRequest {
    @Schema(description = "변경할 동네의 풀네임", example = "서울 동작구 대방동")
    private String regionFullName;
}
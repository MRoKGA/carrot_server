package com.mrokga.carrot_server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Integer id;

    private String email;

    private String nickname;

    private String phoneNumber;

    private String profileImageUrl;

    private Double mannerTemperature;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

package com.mrokga.carrot_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "거래 희망 장소 DTO")
@Getter
public class LocationDto {

    private double latitude;

    private double longitude;

    private String name;
}

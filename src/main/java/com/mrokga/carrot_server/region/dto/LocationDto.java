package com.mrokga.carrot_server.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "거래 희망 장소 DTO")
@Getter
@Setter
public class LocationDto {

    private double latitude;

    private double longitude;

    private String name;
}

package com.mrokga.carrot_server.region.dto;

import com.mrokga.carrot_server.region.entity.Region;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRegionResponse {
    private Integer regionId;
    private String regionName;
    private String regionFullName;
    private Boolean isPrimary;
    private Boolean isActive;
    private LocalDateTime verifiedAt;

    public static UserRegionResponse of(Region r, boolean primary, boolean active, LocalDateTime verifiedAt){
        return UserRegionResponse.builder()
                .regionId(r.getId())
                .regionName(r.getName())
                .regionFullName(r.getFullName())
                .isPrimary(primary)
                .isActive(active)
                .verifiedAt(verifiedAt)
                .build();
    }
}
package com.mrokga.carrot_server.mypage.dto.response;

import com.mrokga.carrot_server.region.entity.Region;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegionSummaryDto", description = "행정동(Region) 기본 정보")
public record RegionSummaryDto(
        @Schema(description = "Region ID", example = "417") Integer id,
        @Schema(description = "동(읍면동) 이름", example = "회기동") String name,
        @Schema(description = "전체 이름(시군구 포함)", example = "서울특별시 동대문구 회기동") String fullName,
        @Schema(description = "행정코드", example = "11000-11230-...") String code,
        @Schema(description = "중심점 좌표(문자열)", example = "POINT(126.92637 37.512554)") String centroid
) {
    public static RegionSummaryDto from(Region r) {
        return new RegionSummaryDto(
                r.getId(),
                r.getName(),
                r.getFullName(),
                r.getCode(),
                r.getCentroid()
        );
    }
}
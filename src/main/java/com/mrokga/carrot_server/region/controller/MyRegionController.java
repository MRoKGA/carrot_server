package com.mrokga.carrot_server.region.controller;

import com.mrokga.carrot_server.api.dto.ApiResponseDto;
import com.mrokga.carrot_server.region.dto.ChangeRegionRequest;
import com.mrokga.carrot_server.region.dto.UserRegionResponse;
import com.mrokga.carrot_server.region.service.UserRegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/region")
@Tag(name = "My Region API", description = "내 동네 관리 API")
public class MyRegionController {

    private final UserRegionService userRegionService;

    @PutMapping
    @Operation(summary = "대표 동네 변경", description = "사용자의 대표(기본) 동네를 변경합니다.")
    public ResponseEntity<ApiResponseDto<UserRegionResponse>> changeMyRegion(
            @RequestParam Integer userId, // ✅ 실제 운영에선 SecurityContext에서 꺼내세요
            @RequestBody ChangeRegionRequest request
    ){
        UserRegionResponse result = userRegionService.changePrimaryRegion(userId, request);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "동네가 변경되었습니다.", result)
        );
    }
}

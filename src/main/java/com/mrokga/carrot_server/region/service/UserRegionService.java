package com.mrokga.carrot_server.region.service;

import com.mrokga.carrot_server.region.dto.ChangeRegionRequest;
import com.mrokga.carrot_server.region.dto.UserRegionResponse;
import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.region.entity.UserRegion;
import com.mrokga.carrot_server.region.repository.RegionRepository;
import com.mrokga.carrot_server.region.repository.UserRegionRepository;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserRegionService {

    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;
    private final UserService userService; // 현재 로그인 사용자 조회용

    /** 대표 동네 변경 (없으면 생성-활성화) */
    @Transactional
    public UserRegionResponse changePrimaryRegion(Integer userId, ChangeRegionRequest req){
        // 1) 유저/지역 조회
        User user = userService.getUserById(userId);
        Region region = regionRepository.findByFullName(req.getRegionFullName())
                .orElseGet(() -> regionRepository.findByName(req.getRegionFullName())
                        .orElseThrow(() -> new IllegalArgumentException("해당 동네가 존재하지 않습니다.")));

        // 2) 기존 대표 해제
        userRegionRepository.findPrimaryByUserId(userId).ifPresent(ur -> {
            ur.setIsPrimary(false);
            ur.setUpdatedAt(LocalDateTime.now());
        });

        // 3) 기존 매핑 있으면 활성/대표로, 없으면 새로 생성
        UserRegion ur = userRegionRepository.findByUserIdAndRegion_Id(userId, region.getId())
                .orElse(UserRegion.builder()
                        .user(user)
                        .region(region)
                        .isActive(true)
                        .isPrimary(true)
                        .verifiedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build());

        ur.setIsActive(true);
        ur.setIsPrimary(true);
        if(ur.getVerifiedAt() == null) ur.setVerifiedAt(LocalDateTime.now());
        ur.setUpdatedAt(LocalDateTime.now());
        userRegionRepository.save(ur);

        return UserRegionResponse.of(region, true, true, ur.getVerifiedAt());
    }
}

package com.mrokga.carrot_server.user.service;

import com.mrokga.carrot_server.auth.dto.request.SignupRequestDto;
import com.mrokga.carrot_server.region.entity.Region;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.region.entity.UserRegion;
import com.mrokga.carrot_server.region.repository.RegionRepository;
import com.mrokga.carrot_server.region.repository.UserRegionRepository;
import com.mrokga.carrot_server.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;

    @Value("${default-profile-image-url}")
    private String defaultProfileImageUrl;

    /**
     * 새로운 사용자를 등록하고, 해당 사용자의 초기 지역 정보 설정
     *
     * @param dto 회원가입 요청 데이터 DTO
     * @return 저장된 User 엔티티
     * @throws EntityNotFoundException 지역 정보를 찾을 수 없을 때 발생
     */
    @Transactional
    public User signup(SignupRequestDto dto) {
        // 1. User 엔티티 생성 및 저장
        User user = User.builder()
                .phoneNumber(dto.getPhoneNumber())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl() != null ? dto.getProfileImageUrl() : defaultProfileImageUrl)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // 2. 지역 정보 조회 및 유효성 검증
        Region region = regionRepository.findByFullName(dto.getRegion()).orElseThrow(() -> new EntityNotFoundException("[UserService.signup] Region not found"));
//        List<UserRegion> userRegionList = userRegionRepository.findAllByUserId(user.getId());

        // 3. UserRegion 엔티티 생성 및 저장
        UserRegion userRegion = UserRegion.builder()
                .user(user)
                .region(region)
//                .isPrimary(userRegionList.isEmpty())
                .isPrimary(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRegionRepository.save(userRegion);

        return user;
    }

    /**
     * 닉네임 중복 여부 리턴
     *
     * @param nickname 확인할 닉네임
     * @return 닉네임이 이미 사용 중이면 true, 아니면 false
     */
    public boolean isDuplicateNickname(String nickname) {
        User user = userRepository.findByNickname(nickname);

        return user != null;
    }

    /**
     * 전화번호로 사용자 조회
     *
     * @param phoneNumber 조회할 전화번호
     * @return 조회된 User 엔티티, 없으면 null 반환
     */
    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }
}

package com.mrokga.carrot_server.User.service;

import com.mrokga.carrot_server.Auth.dto.request.SignupRequestDto;
import com.mrokga.carrot_server.Region.entity.Region;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.Region.entity.UserRegion;
import com.mrokga.carrot_server.Region.repository.RegionRepository;
import com.mrokga.carrot_server.Region.repository.UserRegionRepository;
import com.mrokga.carrot_server.User.repository.UserRepository;
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

    @Transactional
    public User signup(SignupRequestDto dto) {
        User user = User.builder()
                .phoneNumber(dto.getPhoneNumber())
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl() != null ? dto.getProfileImageUrl() : defaultProfileImageUrl)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Region region = regionRepository.findByFullName(dto.getRegion());
//        List<UserRegion> userRegionList = userRegionRepository.findAllByUserId(user.getId());

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

    public boolean isDuplicateNickname(String nickname) {
        User user = userRepository.findByNickname(nickname);

        return user != null;
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
}

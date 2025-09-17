package com.mrokga.carrot_server.mypage.service;

import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.Region.entity.UserRegion;
import com.mrokga.carrot_server.mypage.dto.response.UserRegionDto;
import com.mrokga.carrot_server.mypage.dto.response.UserSummaryDto;
import com.mrokga.carrot_server.mypage.dto.response.UserWithRegionsResponse;
import com.mrokga.carrot_server.Region.repository.UserRegionRepository;
import com.mrokga.carrot_server.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserRegionRepository userRegionRepository;

    @Transactional(readOnly = true)
    public UserWithRegionsResponse getUserWithRegions(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: id=" + userId));

        List<UserRegion> userRegions = userRegionRepository.findAllWithRegionByUserId(userId);

        return new UserWithRegionsResponse(
                UserSummaryDto.from(user),
                userRegions.stream().map(UserRegionDto::from).toList()
        );
    }
}
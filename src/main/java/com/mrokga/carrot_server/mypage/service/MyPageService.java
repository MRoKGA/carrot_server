// src/main/java/.../mypage/service/MyPageService.java
package com.mrokga.carrot_server.mypage.service;

import com.mrokga.carrot_server.chat.entity.Appointment;
import com.mrokga.carrot_server.chat.enums.AppointmentStatus;
import com.mrokga.carrot_server.chat.repository.AppointmentRepository;
import com.mrokga.carrot_server.mypage.dto.AppointmentItemDto;
import com.mrokga.carrot_server.mypage.dto.MyProductDto;
import com.mrokga.carrot_server.mypage.dto.PurchasedItemDto;
import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.repository.FavoriteRepository;
import com.mrokga.carrot_server.product.repository.ProductRepository;

import com.mrokga.carrot_server.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final FavoriteRepository favoriteRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public Page<MyProductDto> getMySellingProducts(Integer userId, Pageable pageable) {
        return productRepository.findMySellingProducts(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PurchasedItemDto> getMyPurchasedProducts(Integer userId, Pageable pageable) {
        return transactionRepository.findPurchasedItemsByBuyerId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MyProductDto> getMyFavoriteProducts(Integer userId, Pageable pageable) {
        return favoriteRepository.findFavoriteProductDtosByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentItemDto> getMyAppointments(Integer userId, AppointmentStatus status, Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findMyAppointments(userId, status, pageable);
        return page.map(a -> {
            Product p = a.getChatRoom().getProduct();
            Integer buyerId = a.getChatRoom().getBuyer().getId();
            Integer sellerId = a.getChatRoom().getSeller().getId();

            // 내 기준 상대방
            boolean iAmBuyer = userId.equals(buyerId);
            var counterpart = iAmBuyer ? a.getChatRoom().getSeller() : a.getChatRoom().getBuyer();

            return AppointmentItemDto.builder()
                    .appointmentId(a.getId())
                    .roomId(a.getChatRoom().getId())
                    .productId(p.getId())
                    .productTitle(p.getTitle())
                    .proposerId(a.getProposer().getId())
                    .proposerNickname(a.getProposer().getNickname())
                    .counterpartId(counterpart.getId())
                    .counterpartNickname(counterpart.getNickname())
                    .meetingTime(a.getMeetingTime())
                    .meetingPlace(a.getMeetingPlace())
                    .status(a.getStatus())
                    .createdAt(a.getCreatedAt())
                    .build();
        });
    }
}

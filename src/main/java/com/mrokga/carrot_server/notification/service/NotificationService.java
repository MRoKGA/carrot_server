package com.mrokga.carrot_server.notification.service;

import com.mrokga.carrot_server.Product.entity.Product;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.notification.entity.Notification;
import com.mrokga.carrot_server.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendCategoryProductNotification(User user, Product product) {

        String title = "상품 등록 알림";
        String content = "관심 카테고리에 새로운 상품 등록: " + product.getTitle();

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .build();

        notificationRepository.save(notification);

        sendNotification(user, notification);
    }

    @Transactional
    public void sendNotification(User user, Notification notification) {
        messagingTemplate.convertAndSend("/sub/notification/" + user.getId(), notification);
    }
}

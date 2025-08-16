package com.mrokga.carrot_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final DefaultMessageService messageService;

    @Value("${sms.sender}")
    private String sender;

    public void sendSms(String phoneNumber) {
        Message message = new Message();
        message.setFrom(sender);
        message.setTo(phoneNumber);
        message.setText("[carrot] 인증번호는 [" + generateCode() + "]입니다.");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException e) {
            log.info("failed message list = {}", e.getFailedMessageList());
            log.info("exception = {}", e.getMessage());
        } catch (Exception e) {
            log.info("exception = {}", e.getMessage());
        }
    }

    /**
     * 6자리 인증번호 생성
     * @return 000000 ~ 999999 범위의 6자리 문자열
     */
    public static String generateCode() {
        int number = (int)(Math.random() * 1000000);
        return String.format("%06d", number);
    }
}

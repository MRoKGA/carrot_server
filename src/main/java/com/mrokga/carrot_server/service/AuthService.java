package com.mrokga.carrot_server.service;

import com.mrokga.carrot_server.enums.VerifyCodeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final DefaultMessageService messageService;

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "sms:code:";
    private static final Duration EXPIRE_MINUTES = Duration.ofMinutes(3);

    @Value("${sms.sender}")
    private String sender;

    public void sendSms(String phoneNumber) {

        String code = generateCode();
        String key = PREFIX + phoneNumber;

        redisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES);

        Message message = new Message();
        message.setFrom(sender);
        message.setTo(phoneNumber);
        message.setText("[carrot] 인증번호는 [" + code + "]입니다.");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException e) {
            log.info("failed message list = {}", e.getFailedMessageList());
            log.info("exception = {}", e.getMessage());
        } catch (Exception e) {
            log.info("exception = {}", e.getMessage());
        }
    }

    public VerifyCodeResult verifyCode(String phoneNumber, String code) {
        log.info("[AuthService] verifyCode starts");
        String key = PREFIX + phoneNumber;

        String saved = redisTemplate.opsForValue().get(key);
        log.info("saved = {}", saved);

        if (saved == null) {
            return VerifyCodeResult.EXPIRED;
        }

        if(!saved.equals(code)) {
            return VerifyCodeResult.MISMATCH;
        }

        log.info("[AuthService] verifyCode finished");

        redisTemplate.delete(key);
        return VerifyCodeResult.OK;

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

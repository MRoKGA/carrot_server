package com.mrokga.carrot_server.auth.service;

import com.mrokga.carrot_server.config.jwt.JwtProperties;
import com.mrokga.carrot_server.config.jwt.TokenProvider;
import com.mrokga.carrot_server.auth.dto.response.TokenResponseDto;
import com.mrokga.carrot_server.user.entity.User;
import com.mrokga.carrot_server.auth.enums.VerifyCodeResult;
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

    private final JwtProperties jwtProperties;

    private final TokenProvider tokenProvider;

    private static final String SMS_PREFIX = "sms:code:";
    private static final Duration SMS_EXPIRE_MINUTES = Duration.ofMinutes(5);

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Value("${sms.sender}")
    private String sender;

    public void sendSms(String phoneNumber) {

        String code = generateCode();
        String key = SMS_PREFIX + phoneNumber;

        redisTemplate.opsForValue().set(key, code, SMS_EXPIRE_MINUTES);

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
        String key = SMS_PREFIX + phoneNumber;

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

    public void resendSms(String phoneNumber) {

        redisTemplate.delete(SMS_PREFIX + phoneNumber);

        sendSms(phoneNumber);
    }

    public TokenResponseDto issueAndReturnTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresInSeconds(jwtProperties.getAccessExpiration().toSeconds())
                .build();
    }

    public TokenResponseDto renew(User user, String oldRefreshToken) {
        String key = REFRESH_TOKEN_PREFIX + user.getId();
        String storedRefreshToken = redisTemplate.opsForValue().get(key);

        if(storedRefreshToken == null || !storedRefreshToken.equals(oldRefreshToken) || !tokenProvider.validToken(oldRefreshToken)) {
            throw new RuntimeException("INVALID REFRESH TOKEN");
        }

        return issueAndReturnTokens(user);
    }

    public void saveRefreshToken(User user, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + user.getId();

        redisTemplate.opsForValue().set(key, refreshToken, jwtProperties.getRefreshExpiration());
    }
}

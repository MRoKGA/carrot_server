package com.mrokga.carrot_server.Auth.service;

import com.mrokga.carrot_server.config.jwt.JwtProperties;
import com.mrokga.carrot_server.config.jwt.TokenProvider;
import com.mrokga.carrot_server.Auth.dto.response.TokenResponseDto;
import com.mrokga.carrot_server.User.entity.User;
import com.mrokga.carrot_server.Auth.enums.VerifyCodeResult;
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

    public static final String DEFAULT_PROFILE_IMAGE =
            "https://media.istockphoto.com/id/1223671392/ko/%EB%B2%A1%ED%84%B0/%EA%B8%B0%EB%B3%B8-%ED%94%84%EB%A1%9C%ED%95%84-%EC%82%AC%EC%A7%84-%EC%95%84%EB%B0%94%ED%83%80-%EC%82%AC%EC%A7%84-%EC%9E%90%EB%A6%AC-%ED%91%9C%EC%8B%9C%EC%9E%90-%EB%B2%A1%ED%84%B0-%EC%9D%BC%EB%9F%AC%EC%8A%A4%ED%8A%B8%EB%A0%88%EC%9D%B4%EC%85%98.jpg?s=612x612&w=0&k=20&c=Z1Yi41x1bDPNjBG5KAn51ZRFfslI4Pz01BOqaRjuzRk=";

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

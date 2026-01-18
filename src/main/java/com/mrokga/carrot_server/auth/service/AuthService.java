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

    // SMS 발신 번호
    @Value("${sms.sender}")
    private String sender;

    /**
     * 지정된 발신번호로 인증번호 SMS를 전송하고, 인증번호를 Redis에 저장
     * @param phoneNumber 인증번호를 받을 전화번호
     */
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
            redisTemplate.delete(key);
            log.info("failed message list = {}", e.getFailedMessageList());
            log.info("exception = {}", e.getMessage());
        } catch (Exception e) {
            redisTemplate.delete(key);
            log.info("exception = {}", e.getMessage());
        }
    }

    /**
     * 사용자가 입력한 인증번호의 유효성 검증
     * @param phoneNumber 전화번호 (Redis Key 조회용)
     * @param code 사용자 입력 인증번호
     * @return 인증 결과 {@link VerifyCodeResult}
     */
    public VerifyCodeResult verifyCode(String phoneNumber, String code) {
        log.info("[AuthService] verifyCode starts");
        String key = SMS_PREFIX + phoneNumber;

        // 1. Redis에서 해당 휴대폰번호로 저장된 인증번호 조회
        String saved = redisTemplate.opsForValue().get(key);
        log.info("saved = {}", saved);

        // 2. 저장된 인증번호가 없는 경우 (만료로 판단)
        if (saved == null) {
            return VerifyCodeResult.EXPIRED;
        }

        // 3. Redis에서 조회한 인증번호와 사용자가 입력한 인증번호가 일치하지 않는 경우
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

    /**
     * 기존 인증번호를 삭제하고 새로운 인증번호 SMS를 재전송
     * @param phoneNumber 인증번호를 받을 전화번호
     */
    public void resendSms(String phoneNumber) {

        redisTemplate.delete(SMS_PREFIX + phoneNumber);

        sendSms(phoneNumber);
    }

    /**
     * Access Token과 Refresh Token을 발급하고, Refresh Token을 Redis에 저장 후 token이 담긴 DTO를 반환
     * @param user 토큰을 발급받을 유저
     * @return 발급된 토큰 정보가 담긴 DTO
     */
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

    /**
     * Refresh Token을 사용하여 Access Token과 Refresh Token 갱신
     * @param user 토큰을 갱신할 유저
     * @param oldRefreshToken 갱신 요청 시 사용될 기존 Refresh Token
     * @return 새롭게 발급된 토큰 정보가 담긴 DTO
     */
    public TokenResponseDto renew(User user, String oldRefreshToken) {
        String key = REFRESH_TOKEN_PREFIX + user.getId();
        String storedRefreshToken = redisTemplate.opsForValue().get(key);

        // 1. 저장된 토큰이 없거나, 요청된 토큰과 저장된 토큰이 일치하지 않거나, 토큰 자체의 유효성 검증에 실패한 경우
        if(storedRefreshToken == null || !storedRefreshToken.equals(oldRefreshToken) || !tokenProvider.validToken(oldRefreshToken)) {
            throw new RuntimeException("INVALID REFRESH TOKEN");
        }

        // 2. 유효한 경우, 새로운 토큰 발급 및 저장
        return issueAndReturnTokens(user);
    }

    /**
     * Refresh Token을 Redis에 저장
     * @param user
     * @param refreshToken 저장할 Refresh Token
     */
    public void saveRefreshToken(User user, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + user.getId();

        redisTemplate.opsForValue().set(key, refreshToken, jwtProperties.getRefreshExpiration());
    }
}

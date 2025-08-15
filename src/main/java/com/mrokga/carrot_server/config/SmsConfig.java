package com.mrokga.carrot_server.config;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfig {

    @Value("${sms.api-key}")
    private String apiKey;

    @Value("${sms.api-secret-key}")
    private String apiSecretKey;

    @Bean
    public DefaultMessageService defaultMessageService() {
        return NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.solapi.com");
    }
}

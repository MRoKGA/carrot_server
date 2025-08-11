package com.mrokga.carrot_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/theTest").permitAll()   // 테스트 엔드포인트 허용
                .anyRequest().authenticated()
        );
        http.httpBasic(Customizer.withDefaults());     // 나머지는 Basic 인증 유지
        return http.build();
    }
}

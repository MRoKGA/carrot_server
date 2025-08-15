package com.mrokga.carrot_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 끄기
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/theTest").permitAll() // theTest는 전부 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/auth/*").permitAll()
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .httpBasic(httpBasic -> {}); // Basic 인증 유지

        return http.build();
    }
}

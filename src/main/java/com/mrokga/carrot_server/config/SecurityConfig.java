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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 콘솔 같은 프레임 허용시
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                                "/actuator/health", "/actuator/info",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/theTest"
                 ).permitAll()
                 .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())   // 임시(나중에 JWT로 교체)
                .formLogin(form -> form.disable())
                .logout(l -> l.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS));

        return http.build();
    }
}


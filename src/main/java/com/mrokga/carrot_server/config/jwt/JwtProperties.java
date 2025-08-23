package com.mrokga.carrot_server.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("jwt")
public class JwtProperties {

    private String issuer;

    private String secret;

    private Duration accessExpiration;

    private Duration refreshExpiration;
}



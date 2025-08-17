package com.mrokga.carrot_server.config.jwt;

import com.mrokga.carrot_server.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        return generateToken(user, new Date(now.getTime() + jwtProperties.getAccessExpiration().toMillis()));
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        return generateToken(user, new Date(now.getTime() + jwtProperties.getRefreshExpiration().toMillis()));
    }

    public String generateToken(User user, Date expiration) {

        JwtBuilder builder = Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(expiration)
                .subject(String.valueOf(user.getId()))
                .signWith(key, Jwts.SIG.HS256);

        if(user.getEmail() != null) {
            builder.claim("email", user.getEmail());
        }

        return builder.compact();
    }

    public boolean validToken(String token) {
        try {
            Jwts.parser().requireIssuer(jwtProperties.getIssuer()).verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | SecurityException | MalformedJwtException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        var claims = getClaims(token);
        var authorities = java.util.Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        var principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    private Claims getClaims(String token) {
        return Jwts.parser().requireIssuer(jwtProperties.getIssuer())
                .verifyWith(key).build().parseSignedClaims(token)
                .getPayload();
    }
}
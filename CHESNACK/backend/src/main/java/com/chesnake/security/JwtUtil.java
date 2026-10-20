package com.chesnake.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 生成与解析（HS256）
 */
@Component
public class JwtUtil {

    @Value("${chesnake.jwt.secret}")
    private String secret;

    @Value("${chesnake.jwt.expire-minutes:120}")
    private long expireMinutes;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(String email, String username, Integer role) {
        Date now = new Date();
        return Jwts.builder()
                // jti 唯一标识：同一秒内重复登录生成的 token 也必须不同，
                // 否则退出登录后立即重登，新 token 会被旧 token 的黑名单误杀
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMinutes * 60_000L))
                .signWith(key())
                .compact();
    }

    /**
     * 解析 Token，非法/过期抛出异常
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * token 剩余有效秒数（已过期或非法返回 0）
     */
    public long remainingSeconds(String token) {
        try {
            Claims claims = parse(token);
            return Math.max(0, (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }
}

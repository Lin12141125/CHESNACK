package com.chesnake.security;

import com.chesnake.common.CacheConsts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * JWT 主动吊销：退出登录时把 token 加入 Redis 黑名单（TTL=token 剩余有效期），
 * 解决无状态 JWT 无法主动失效的问题。
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedis;
    private final JwtUtil jwtUtil;

    public void blacklist(String token) {
        long remain = jwtUtil.remainingSeconds(token);
        if (remain <= 0) {
            return;
        }
        stringRedis.opsForValue().set(key(token), "1", remain, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedis.hasKey(key(token)));
    }

    private String key(String token) {
        return CacheConsts.JWT_BLACKLIST + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}

package com.chesnake.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JWT 黑名单测试：退出登录吊销（TTL=剩余有效期）与查询
 */
class TokenBlacklistServiceTest {

    private final StringRedisTemplate stringRedis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final TokenBlacklistService service = new TokenBlacklistService(stringRedis, jwtUtil);

    @Test
    void blacklist_writesWithRemainingTtl() {
        when(jwtUtil.remainingSeconds("token-x")).thenReturn(600L);
        when(stringRedis.opsForValue()).thenReturn(valueOps);

        service.blacklist("token-x");

        verify(valueOps).set(anyString(), eq("1"), eq(600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void blacklist_expiredToken_skipped() {
        when(jwtUtil.remainingSeconds("token-x")).thenReturn(0L);

        service.blacklist("token-x");

        verify(stringRedis, never()).opsForValue();
    }

    @Test
    void isBlacklisted_delegatesToRedis() {
        when(stringRedis.hasKey(anyString())).thenReturn(true);

        assertThat(service.isBlacklisted("token-x")).isTrue();
    }
}

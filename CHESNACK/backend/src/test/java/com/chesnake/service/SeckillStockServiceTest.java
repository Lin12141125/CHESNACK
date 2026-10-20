package com.chesnake.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 秒杀库存服务测试：Lua 预扣成功/失败、回滚、预热与覆盖同步
 */
class SeckillStockServiceTest {

    private final StringRedisTemplate stringRedis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    private final SeckillStockService service = new SeckillStockService(stringRedis);

    private final List<SeckillStockService.StockItem> items = List.of(
            new SeckillStockService.StockItem(1L, 2),
            new SeckillStockService.StockItem(2L, 3));

    @Test
    void tryDeduct_success_callsLuaWithKeysAndArgs() {
        when(stringRedis.execute(any(DefaultRedisScript.class), anyList(), any(String[].class)))
                .thenReturn(1L);

        boolean result = service.tryDeduct(items);

        assertThat(result).isTrue();
        verify(stringRedis).execute(any(DefaultRedisScript.class),
                eq(List.of("seckill:stock:1", "seckill:stock:2")),
                eq(new String[]{"2", "3"}));
    }

    @Test
    void tryDeduct_insufficient_returnsFalse() {
        when(stringRedis.execute(any(DefaultRedisScript.class), anyList(), any(String[].class)))
                .thenReturn(-1L);

        assertThat(service.tryDeduct(items)).isFalse();
    }

    @Test
    void tryDeduct_emptyItems_returnsFalse() {
        assertThat(service.tryDeduct(List.of())).isFalse();
        verify(stringRedis, never()).execute(any(), anyList(), any(String[].class));
    }

    @Test
    void rollback_callsLua() {
        when(stringRedis.execute(any(DefaultRedisScript.class), anyList(), any(String[].class)))
                .thenReturn(1L);

        service.rollback(items);

        verify(stringRedis).execute(any(DefaultRedisScript.class),
                eq(List.of("seckill:stock:1", "seckill:stock:2")),
                eq(new String[]{"2", "3"}));
    }

    @Test
    void preheatIfAbsent_usesSetIfAbsent() {
        when(stringRedis.opsForValue()).thenReturn(valueOps);

        service.preheatIfAbsent(1L, 100);

        verify(valueOps).setIfAbsent("seckill:stock:1", "100");
    }

    @Test
    void syncStock_overwritesValue() {
        when(stringRedis.opsForValue()).thenReturn(valueOps);

        service.syncStock(1L, 50);

        verify(valueOps).set("seckill:stock:1", "50");
    }

    @Test
    void preheatIfAbsent_invalidInput_ignored() {
        service.preheatIfAbsent(1L, -1);
        verify(stringRedis, never()).opsForValue();
    }
}

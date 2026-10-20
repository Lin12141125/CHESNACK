package com.chesnake.service;

import com.chesnake.common.CacheConsts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 缓存延迟双删（优化7）：
 * 独立组件持有 @Async 方法（避免 @Async 自调用失效问题）。
 *
 * 「双删」策略：写库前删一次（清理旧缓存）→ 写库 → 写库后删一次 → 延迟 500ms 再删一次。
 * 延迟删除兜底并发场景：读请求在写库间隙把旧数据写回缓存的窗口期。
 * 说明：更彻底的方案是订阅 binlog（Canal）异步更新缓存，双删是轻量级替代。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDoubleDeleteService {

    private final StringRedisTemplate stringRedis;

    /**
     * 延迟双删：500ms 后再次失效详情与列表缓存
     */
    @Async("cacheEvictExecutor")
    public void delayEvict(Long goodId) {
        sleep(500);
        try {
            stringRedis.delete(CacheConsts.GOODS_DETAIL + goodId);
            GoodsService.evictListCacheByScan(stringRedis);
        } catch (Exception e) {
            log.warn("延迟双删执行失败: goodId={}", goodId, e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

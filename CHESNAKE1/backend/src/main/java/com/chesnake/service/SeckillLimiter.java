package com.chesnake.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀信号量（优化5）：限制「同时在途」的下单请求数。
 *
 * 与 Sentinel QPS 限流互补：Sentinel 控制每秒放行量，信号量控制同时处理中的数量
 * （防止大量慢请求堆积拖垮 DB 连接池/线程池）。
 */
@Component
public class SeckillLimiter {

    private final Semaphore semaphore;

    public SeckillLimiter(@Value("${chesnake.seckill.max-concurrent:200}") int maxConcurrent) {
        this.semaphore = new Semaphore(Math.max(1, maxConcurrent));
    }

    /** 尝试获取令牌，超时返回 false */
    public boolean tryAcquire(long timeoutMs) {
        try {
            return semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void release() {
        semaphore.release();
    }
}

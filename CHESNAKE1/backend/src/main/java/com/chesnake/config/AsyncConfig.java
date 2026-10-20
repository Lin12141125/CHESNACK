package com.chesnake.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步与定时任务配置：
 * - @EnableAsync：延迟双删、ES 异步同步等
 * - @EnableScheduling：MQ 补偿任务
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Bean("cacheEvictExecutor")
    public Executor cacheEvictExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("cache-evict-");
        executor.initialize();
        return executor;
    }

    @Bean("esSyncExecutor")
    public Executor esSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("es-sync-");
        executor.initialize();
        return executor;
    }
}

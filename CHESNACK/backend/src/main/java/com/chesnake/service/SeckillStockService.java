package com.chesnake.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 秒杀库存服务（优化5）
 *
 * 库存模型：DB 为唯一权威，Redis 中 `seckill:stock:{goodId}` 为「预扣视图」：
 * 1. 应用启动时全量预热（SETNX，不覆盖运行期已预扣的值）
 * 2. 管理员变更商品时覆盖同步
 * 3. 下单入口用 Lua 脚本对多商品原子「检查+预扣」，任一不足整体回滚，避免逐条命令的竞态
 * 4. 消费者落库后 DB 扣减为最终防线；DB 扣减失败（库存不一致）回补 Redis 预扣
 *
 * 高并发下把「库存判断」压力从 DB 移到 Redis，DB 只做最终持久化扣减。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillStockService {

    /** 预扣库存 key 前缀 */
    public static final String STOCK_KEY_PREFIX = "seckill:stock:";

    private final StringRedisTemplate stringRedis;

    /**
     * Lua：多商品原子「检查+预扣」。任一商品库存不足时回滚前面已扣部分并返回 -1
     * KEYS   = [stockKey1, stockKey2, ...]
     * ARGV   = [num1, num2, ...] 与 KEYS 一一对应
     * 返回   = 1 成功  -1 库存不足
     */
    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT = new DefaultRedisScript<>(
            "for i = 1, #KEYS do\n" +
            "  local stock = tonumber(redis.call('get', KEYS[i]))\n" +
            "  local num = tonumber(ARGV[i])\n" +
            "  if stock == nil or stock < num then\n" +
            "    for j = 1, i - 1 do\n" +
            "      redis.call('incrby', KEYS[j], tonumber(ARGV[j]))\n" +
            "    end\n" +
            "    return -1\n" +
            "  end\n" +
            "end\n" +
            "for i = 1, #KEYS do\n" +
            "  redis.call('decrby', KEYS[i], tonumber(ARGV[i]))\n" +
            "end\n" +
            "return 1",
            Long.class);

    /** Lua：回滚预扣（消息投递失败 / DB 扣减失败时回补） */
    private static final DefaultRedisScript<Long> ROLLBACK_SCRIPT = new DefaultRedisScript<>(
            "for i = 1, #KEYS do\n" +
            "  redis.call('incrby', KEYS[i], tonumber(ARGV[i]))\n" +
            "end\n" +
            "return 1",
            Long.class);

    /**
     * 原子预扣多商品库存。成功返回 true；任一不足自动回滚并返回 false。
     */
    public boolean tryDeduct(List<StockItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        List<String> keys = items.stream().map(i -> STOCK_KEY_PREFIX + i.goodId()).toList();
        List<String> args = items.stream().map(i -> String.valueOf(i.amount())).toList();
        Long result = stringRedis.execute(DEDUCT_SCRIPT, keys, args.toArray(new String[0]));
        return result != null && result == 1L;
    }

    /** 回滚预扣（消息投递失败 / DB 扣减失败时回补） */
    public void rollback(List<StockItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<String> keys = items.stream().map(i -> STOCK_KEY_PREFIX + i.goodId()).toList();
        List<String> args = items.stream().map(i -> String.valueOf(i.amount())).toList();
        try {
            stringRedis.execute(ROLLBACK_SCRIPT, keys, args.toArray(new String[0]));
        } catch (Exception e) {
            log.error("回滚预扣库存失败: {}", items, e);
        }
    }

    /** 预热：key 不存在才写入（应用启动全量预热，避免覆盖运行期已预扣的值） */
    public void preheatIfAbsent(Long goodId, Integer stock) {
        if (goodId == null || stock == null || stock < 0) {
            return;
        }
        stringRedis.opsForValue().setIfAbsent(STOCK_KEY_PREFIX + goodId, String.valueOf(stock));
    }

    /** 覆盖同步（管理员新增/修改商品时，DB 为权威） */
    public void syncStock(Long goodId, Integer stock) {
        if (goodId == null || stock == null || stock < 0) {
            return;
        }
        stringRedis.opsForValue().set(STOCK_KEY_PREFIX + goodId, String.valueOf(stock));
    }

    /** 读取当前预扣视图中的剩余库存（可空） */
    public Long remaining(Long goodId) {
        String v = stringRedis.opsForValue().get(STOCK_KEY_PREFIX + goodId);
        return v == null ? null : Long.parseLong(v);
    }

    /** 删除库存 key（商品下架） */
    public void removeStock(Long goodId) {
        if (goodId == null) {
            return;
        }
        stringRedis.delete(STOCK_KEY_PREFIX + goodId);
    }

    /** 预扣条目 */
    public record StockItem(Long goodId, Integer amount) {
    }

    /** 批量预扣（便捷方法） */
    public List<StockItem> toItems(List<Long> goodIds, List<Integer> amounts) {
        List<StockItem> items = new ArrayList<>();
        for (int i = 0; i < goodIds.size(); i++) {
            items.add(new StockItem(goodIds.get(i), amounts.get(i)));
        }
        return items;
    }
}

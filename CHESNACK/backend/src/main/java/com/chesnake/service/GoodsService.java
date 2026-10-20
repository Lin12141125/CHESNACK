package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.BusinessException;
import com.chesnake.common.CacheConsts;
import com.chesnake.common.PageResult;
import com.chesnake.dto.GoodsQueryDTO;
import com.chesnake.entity.Goods;
import com.chesnake.mapper.GoodsMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务：缓存击穿/穿透/雪崩 三种场景的防护实现
 *
 * 防穿透：参数校验 + 空值缓存（短 TTL）
 * 防击穿：Redis SETNX 互斥锁 + 双重校验 + 等待重试
 * 防雪崩：过期时间 = 基础 TTL + 随机偏移
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsService {

    private static final Set<String> SORT_FIELDS = Set.of("score", "price", "sale_quantity");

    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate stringRedis;
    private final ObjectMapper objectMapper;
    private final CacheDoubleDeleteService cacheDoubleDeleteService;

    // ================= 商品详情 =================

    public Goods detail(Long id) {
        // ① 防穿透：参数合法性校验，非法 ID 直接拦截，不查库
        if (id == null || id <= 0) {
            throw new BusinessException("商品ID非法");
        }
        String key = CacheConsts.GOODS_DETAIL + id;

        // 查缓存
        Goods cached = readCache(key);
        if (cached != null) return cached;
        if (isNullCached(key)) throw new BusinessException("商品不存在");

        // ② 防击穿：缓存未命中时加互斥锁，只有一个线程回源重建
        String lockKey = CacheConsts.GOODS_LOCK + id;
        if (tryLock(lockKey)) {
            try {
                // 双重校验：拿到锁后再查一次缓存
                cached = readCache(key);
                if (cached != null) return cached;
                if (isNullCached(key)) throw new BusinessException("商品不存在");

                Goods goods = goodsMapper.selectById(id);
                writeDetail(key, goods);
                if (goods == null) throw new BusinessException("商品不存在");
                return goods;
            } finally {
                unlock(lockKey);
            }
        }

        // 没抢到锁：等待持锁线程重建，短暂重试读缓存
        for (int i = 0; i < 3; i++) {
            sleep(50);
            cached = readCache(key);
            if (cached != null) return cached;
            if (isNullCached(key)) throw new BusinessException("商品不存在");
        }
        // 兜底：直接查库返回（不写缓存，防止并发重建加剧雪崩）
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) throw new BusinessException("商品不存在");
        return goods;
    }

    // ================= 商品列表（筛选/排序/分页） =================

    public PageResult<Goods> page(GoodsQueryDTO q) {
        String key = CacheConsts.GOODS_LIST + listKey(q);

        PageResult<Goods> cached = readList(key);
        if (cached != null) return cached;
        if (isNullCached(key)) return new PageResult<>(List.of(), 0);

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.keyword())) {
            wrapper.like(Goods::getName, q.keyword());
        }
        if (StringUtils.hasText(q.brand())) {
            wrapper.eq(Goods::getBrand, q.brand());
        }
        if (StringUtils.hasText(q.type())) {
            wrapper.eq(Goods::getType, q.type());
        }
        if (StringUtils.hasText(q.origin())) {
            wrapper.eq(Goods::getOrigin, q.origin());
        }
        // 排序字段白名单，防止 SQL 注入
        String field = SORT_FIELDS.contains(q.field()) ? q.field() : "score";
        String dir = "asc".equalsIgnoreCase(q.direction()) ? "ASC" : "DESC";
        wrapper.last("ORDER BY " + field + " " + dir);

        Page<Goods> result = goodsMapper.selectPage(new Page<>(q.page(), q.size()), wrapper);
        PageResult<Goods> pageResult = new PageResult<>(result.getRecords(), result.getTotal());

        // ③ 防雪崩：列表缓存 TTL 加随机偏移；空结果走空值缓存（防穿透）
        if (result.getRecords().isEmpty()) {
            stringRedis.opsForValue().set(key, CacheConsts.NULL_PLACEHOLDER,
                    CacheConsts.NULL_TTL_MINUTES, TimeUnit.MINUTES);
        } else {
            writeList(key, pageResult);
        }
        return pageResult;
    }

    /**
     * 搜索降级兜底（优化8）：ES 不可用时回退 MySQL LIKE 多字段模糊查询
     * field/direction 排序参数与列表接口一致（白名单防注入）
     */
    public PageResult<Goods> searchByMysql(String keyword, int page, int size, String field, String direction) {
        // field 可为 null（搜索接口参数可选）：Set.of 不可变集合 contains(null) 会抛 NPE，须先判空
        String sortField = (field != null && SORT_FIELDS.contains(field)) ? field : "score";
        String dir = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .and(w -> w.like(Goods::getName, keyword)
                        .or().like(Goods::getBrand, keyword)
                        .or().like(Goods::getType, keyword)
                        .or().like(Goods::getOrigin, keyword))
                .last("ORDER BY " + sortField + " " + dir);
        Page<Goods> result = goodsMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    /** 筛选面板选项（品牌/类型/产地） */
    public Map<String, List<String>> filters() {
        String key = "cache:goods:filters";
        String cached = stringRedis.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<>() {});
            } catch (Exception ignored) {
            }
        }
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("brands", goodsMapper.selectBrands());
        map.put("types", goodsMapper.selectTypes());
        map.put("origins", goodsMapper.selectOrigins());
        try {
            long ttl = 30 + ThreadLocalRandom.current().nextLong(11); // 随机 TTL
            stringRedis.opsForValue().set(key, objectMapper.writeValueAsString(map), ttl, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
        return map;
    }

    // ================= 缓存维护 =================

    public void evictDetail(Long id) {
        stringRedis.delete(CacheConsts.GOODS_DETAIL + id);
    }

    /**
     * 失效全部列表缓存：使用 SCAN 游标迭代，避免生产环境 KEYS * 阻塞 Redis
     */
    public void evictListCache() {
        evictListCacheByScan(stringRedis);
    }

    /** 供延迟双删等独立组件复用 */
    static void evictListCacheByScan(StringRedisTemplate stringRedis) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(CacheConsts.GOODS_LIST + "*")
                .count(200)
                .build();
        try (Cursor<String> cursor = stringRedis.scan(options)) {
            while (cursor.hasNext()) {
                stringRedis.delete(cursor.next());
            }
        } catch (Exception e) {
            log.warn("SCAN 清理列表缓存失败", e);
        }
    }

    public void evictAll(Long id) {
        evictDetail(id);
        evictListCache();
    }

    /** 批量失效详情缓存（下单扣库存/销量变化后保证缓存一致性） */
    public void evictDetails(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<String> keys = ids.stream().map(id -> CacheConsts.GOODS_DETAIL + id).toList();
        stringRedis.delete(keys);
    }

    /**
     * 双删第一步：写库前删除（更新场景：先清旧缓存，防止并发读把旧值写回）
     */
    public void deleteBeforeUpdate(Long id) {
        if (id == null) return;
        evictAll(id);
    }

    /**
     * 双删第二步：写库后立即删除 + 延迟双删（500ms 兜底并发窗口）
     */
    public void deleteAfterUpdate(Long id) {
        if (id == null) return;
        evictAll(id);
        cacheDoubleDeleteService.delayEvict(id);
    }

    // ================= 内部工具 =================

    private Goods readCache(String key) {
        String json = stringRedis.opsForValue().get(key);
        if (json == null || CacheConsts.NULL_PLACEHOLDER.equals(json)) return null;
        try {
            return objectMapper.readValue(json, Goods.class);
        } catch (Exception e) {
            // 脏数据：删除，让后续请求重建
            stringRedis.delete(key);
            return null;
        }
    }

    private boolean isNullCached(String key) {
        String v = stringRedis.opsForValue().get(key);
        return CacheConsts.NULL_PLACEHOLDER.equals(v);
    }

    private void writeDetail(String key, Goods goods) {
        if (goods == null) {
            // 空值缓存，防穿透
            stringRedis.opsForValue().set(key, CacheConsts.NULL_PLACEHOLDER,
                    CacheConsts.NULL_TTL_MINUTES, TimeUnit.MINUTES);
            return;
        }
        // 基础 TTL + 随机偏移，防雪崩
        long ttl = CacheConsts.GOODS_DETAIL_TTL_MINUTES
                + ThreadLocalRandom.current().nextLong(CacheConsts.GOODS_DETAIL_TTL_RANDOM_MINUTES + 1);
        try {
            stringRedis.opsForValue().set(key, objectMapper.writeValueAsString(goods), ttl, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写详情缓存失败: {}", key, e);
        }
    }

    private PageResult<Goods> readList(String key) {
        String json = stringRedis.opsForValue().get(key);
        if (json == null || CacheConsts.NULL_PLACEHOLDER.equals(json)) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<PageResult<Goods>>() {});
        } catch (Exception e) {
            stringRedis.delete(key);
            return null;
        }
    }

    private void writeList(String key, PageResult<Goods> page) {
        long ttl = CacheConsts.GOODS_LIST_TTL_MINUTES
                + ThreadLocalRandom.current().nextLong(CacheConsts.GOODS_LIST_TTL_RANDOM_MINUTES + 1);
        try {
            stringRedis.opsForValue().set(key, objectMapper.writeValueAsString(page), ttl, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写列表缓存失败: {}", key, e);
        }
    }

    private String listKey(GoodsQueryDTO q) {
        return (q.keyword() == null ? "" : q.keyword()) + "|" +
                (q.brand() == null ? "" : q.brand()) + "|" +
                (q.type() == null ? "" : q.type()) + "|" +
                (q.origin() == null ? "" : q.origin()) + "|" +
                q.field() + "|" + q.direction() + "|" + q.page() + "|" + q.size();
    }

    /** 当前线程持有的锁值（UUID），用于安全释放锁 */
    private final ThreadLocal<String> lockToken = new ThreadLocal<>();

    /** 原子解锁 Lua：比对锁值一致才删除，防止误删他人锁 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private boolean tryLock(String key) {
        String token = UUID.randomUUID().toString();
        Boolean ok = stringRedis.opsForValue().setIfAbsent(key, token,
                CacheConsts.LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(ok)) {
            lockToken.set(token);
            return true;
        }
        return false;
    }

    private void unlock(String key) {
        String token = lockToken.get();
        if (token == null) return;
        stringRedis.execute(UNLOCK_SCRIPT, List.of(key), token);
        lockToken.remove();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

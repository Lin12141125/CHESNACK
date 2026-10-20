package com.chesnake.config;

import com.chesnake.entity.Goods;
import com.chesnake.mapper.GoodsMapper;
import com.chesnake.search.GoodsSearchService;
import com.chesnake.service.SeckillStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动预热（优化5+8）：
 * - Redis 预扣库存视图全量预热（SETNX：不覆盖运行期已预扣的值）
 * - Elasticsearch 索引异步全量重建（失败不影响启动）
 * - 定时兜底预热：Redis 重启/数据丢失后 60s 内自动补齐库存视图
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupPreheatRunner implements ApplicationRunner {

    private final GoodsMapper goodsMapper;
    private final SeckillStockService stockService;
    private final GoodsSearchService goodsSearchService;

    @Override
    public void run(ApplicationArguments args) {
        preheatStocks();
        // ES 索引重建（异步，搜索引擎不可用自动降级 MySQL）
        try {
            goodsSearchService.rebuild();
        } catch (Exception e) {
            log.warn("[预热] ES 重建触发失败: {}", e.getMessage());
        }
    }

    /** 定时兜底：每 60s 检查并补齐缺失的库存预热 key（SETNX 幂等，成本低） */
    @Scheduled(fixedDelay = 60_000)
    public void scheduledPreheat() {
        preheatStocks();
        // ES 索引兜底：搜索引擎晚于应用就绪时自动补建（rebuild 幂等）
        try {
            goodsSearchService.rebuild();
        } catch (Exception e) {
            log.warn("[预热] ES 兜底重建触发失败: {}", e.getMessage());
        }
    }

    private void preheatStocks() {
        try {
            List<Goods> all = goodsMapper.selectList(null);
            int missing = 0;
            for (Goods goods : all) {
                if (stockService.remaining(goods.getId()) == null) {
                    stockService.preheatIfAbsent(goods.getId(), goods.getStock());
                    missing++;
                }
            }
            if (missing > 0) {
                log.info("[预热] 补齐库存视图 {} 个商品（总计 {}）", missing, all.size());
            }
        } catch (Exception e) {
            log.warn("[预热] Redis 库存预热失败（Redis 未就绪?）: {}", e.getMessage());
        }
    }
}

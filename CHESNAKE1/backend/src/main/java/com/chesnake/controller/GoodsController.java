package com.chesnake.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.chesnake.common.PageResult;
import com.chesnake.common.R;
import com.chesnake.config.SentinelConfig;
import com.chesnake.dto.GoodsQueryDTO;
import com.chesnake.entity.Goods;
import com.chesnake.search.GoodsSearchService;
import com.chesnake.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final GoodsSearchService goodsSearchService;

    /** 商品列表（关键词/品牌/类型/产地筛选 + 排序 + 分页），走列表缓存 */
    @GetMapping
    public R<PageResult<Goods>> list(GoodsQueryDTO query) {
        return R.ok(goodsService.page(query));
    }

    /** 商品全文搜索（Elasticsearch 多字段加权，不可用自动降级 MySQL）；支持排序 */
    @GetMapping("/search")
    public R<PageResult<Goods>> search(@RequestParam String keyword,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       @RequestParam(required = false) String field,
                                       @RequestParam(required = false) String direction) {
        return R.ok(goodsSearchService.search(keyword, page, size, field, direction));
    }

    /** 商品详情，走"击穿/穿透/雪崩"三级防护缓存；QPS 100 限流 */
    @SentinelResource(value = SentinelConfig.RES_GOODS_DETAIL, blockHandler = "detailBlockHandler")
    @GetMapping("/{id}")
    public R<Goods> detail(@PathVariable Long id) {
        return R.ok(goodsService.detail(id));
    }

    /** Sentinel 限流兜底：返回 429 语义错误码，前端提示稍后再试 */
    public R<Goods> detailBlockHandler(Long id, BlockException e) {
        return R.fail(429, "访问过于频繁，请稍后再试");
    }

    /** 筛选面板选项 */
    @GetMapping("/filters")
    public R<Map<String, List<String>>> filters() {
        return R.ok(goodsService.filters());
    }
}

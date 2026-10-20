package com.chesnake.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.chesnake.common.PageResult;
import com.chesnake.entity.Goods;
import com.chesnake.mapper.GoodsMapper;
import com.chesnake.service.GoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 商品全文搜索（优化8）：
 * - 多字段加权检索（name > brand > type/origin）+ 销量排序
 * - ES 不可用时自动降级 MySQL LIKE，保证搜索可用性
 * - 索引数据同步：应用启动全量 + 管理员增删改增量
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsSearchService {

    private final ElasticsearchClient esClient;
    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;

    /** 排序字段白名单（与列表接口一致，防止注入；sale_quantity 映射 ES 字段 saleQuantity） */
    private static final Set<String> SORT_FIELDS = Set.of("score", "price", "sale_quantity");

    /**
     * 商品搜索（带降级）：ES 异常时回退 MySQL LIKE；field/direction 控制排序
     */
    public PageResult<Goods> search(String keyword, int page, int size, String field, String direction) {
        try {
            return searchInEs(keyword, page, size, field, direction);
        } catch (Exception e) {
            log.warn("[ES降级] 搜索引擎不可用，降级 MySQL LIKE: {}", e.getMessage());
            return goodsService.searchByMysql(keyword, page, size, field, direction);
        }
    }

    private PageResult<Goods> searchInEs(String keyword, int page, int size, String field, String direction) throws Exception {
        // field 可为 null（接口参数可选）：Set.of 不可变集合 contains(null) 会抛 NPE，须先判空
        String sortField = (field != null && SORT_FIELDS.contains(field)) ? field : null;
        SortOrder order = "asc".equalsIgnoreCase(direction) ? SortOrder.Asc : SortOrder.Desc;
        SearchResponse<GoodsDoc> response = esClient.search(s -> {
            s.index(GoodsDoc.INDEX)
                    .from((page - 1) * size)
                    .size(size)
                    .query(q -> q.bool(b -> b
                            .should(m -> m.match(mm -> mm.field("name").query(keyword).boost(3.0f)))
                            .should(m -> m.match(mm -> mm.field("brand").query(keyword).boost(2.0f)))
                            .should(m -> m.match(mm -> mm.field("type").query(keyword)))
                            .should(m -> m.match(mm -> mm.field("origin").query(keyword)))
                            .minimumShouldMatch("1")));
            if (sortField == null) {
                // 未指定排序：相关度优先，销量兜底
                s.sort(so -> so.score(sc -> sc.order(SortOrder.Desc)));
                s.sort(so -> so.field(f -> f.field("saleQuantity").order(SortOrder.Desc)));
            } else {
                String esField = "sale_quantity".equals(sortField) ? "saleQuantity" : sortField;
                s.sort(so -> so.field(f -> f.field(esField).order(order)));
            }
            return s;
        }, GoodsDoc.class);

        long total = response.hits().total() == null ? 0 : response.hits().total().value();
        List<Goods> records = response.hits().hits().stream()
                .filter(h -> h.source() != null)
                .map(h -> toGoods(h.source()))
                .toList();
        return new PageResult<>(records, total);
    }

    private Goods toGoods(GoodsDoc doc) {
        Goods goods = new Goods();
        goods.setId(doc.getId());
        goods.setName(doc.getName());
        goods.setBrand(doc.getBrand());
        goods.setType(doc.getType());
        goods.setOrigin(doc.getOrigin());
        goods.setPrice(doc.getPrice());
        goods.setSaleQuantity(doc.getSaleQuantity());
        goods.setStock(doc.getStock());
        goods.setScore(doc.getScore());
        goods.setImg1(doc.getImg1());
        return goods;
    }

    /** 单条同步（管理员增改商品后调用） */
    @Async("esSyncExecutor")
    public void syncOne(Goods goods) {
        try {
            esClient.index(i -> i.index(GoodsDoc.INDEX).id(String.valueOf(goods.getId())).document(toDoc(goods)));
        } catch (Exception e) {
            log.warn("[ES] 同步商品 {} 失败: {}", goods.getId(), e.getMessage());
        }
    }

    /** 单条删除（管理员删商品后调用） */
    @Async("esSyncExecutor")
    public void deleteOne(Long id) {
        try {
            esClient.delete(d -> d.index(GoodsDoc.INDEX).id(String.valueOf(id)));
        } catch (Exception e) {
            log.warn("[ES] 删除商品 {} 失败: {}", id, e.getMessage());
        }
    }

    /**
     * 全量重建索引 + 同步（幂等：索引已存在则跳过，可安全被定时任务重复调用）
     */
    @Async("esSyncExecutor")
    public void rebuild() {
        try {
            // 索引已存在（且非空）则不重复重建
            boolean exists = esClient.indices().exists(e -> e.index(GoodsDoc.INDEX)).value();
            if (exists) {
                long count = esClient.count(c -> c.index(GoodsDoc.INDEX)).count();
                if (count > 0) {
                    return;
                }
            }
            // 索引不存在则创建（text 字段默认标准分词；生产可挂 IK 分词插件）
            if (!exists) {
                esClient.indices().create(c -> c.index(GoodsDoc.INDEX).mappings(m -> m
                        .properties("id", p -> p.long_(x -> x))
                        .properties("name", p -> p.text(t -> t))
                        .properties("brand", p -> p.text(t -> t))
                        .properties("type", p -> p.text(t -> t))
                        .properties("origin", p -> p.text(t -> t))
                        .properties("price", p -> p.scaledFloat(x -> x.scalingFactor(100.0)))
                        .properties("saleQuantity", p -> p.integer(x -> x))
                        .properties("stock", p -> p.integer(x -> x))
                        .properties("score", p -> p.integer(x -> x))
                        .properties("img1", p -> p.keyword(k -> k))));
            }

            List<Goods> all = goodsMapper.selectList(null);
            if (all.isEmpty()) {
                return;
            }
            BulkRequest.Builder bulk = new BulkRequest.Builder();
            for (Goods goods : all) {
                bulk.operations(op -> op.index(i -> i
                        .index(GoodsDoc.INDEX)
                        .id(String.valueOf(goods.getId()))
                        .document(toDoc(goods))));
            }
            BulkResponse response = esClient.bulk(bulk.build());
            long failed = response.items().stream().filter(i -> i.error() != null).count();
            log.info("[ES] 全量同步完成: 总数={}, 失败={}", all.size(), failed);
        } catch (Exception e) {
            log.warn("[ES] 全量同步失败（搜索引擎未就绪，不影响业务）: {}", e.getMessage());
        }
    }

    private GoodsDoc toDoc(Goods goods) {
        GoodsDoc doc = new GoodsDoc();
        doc.setId(goods.getId());
        doc.setName(goods.getName());
        doc.setBrand(goods.getBrand());
        doc.setType(goods.getType());
        doc.setOrigin(goods.getOrigin());
        doc.setPrice(goods.getPrice());
        doc.setSaleQuantity(goods.getSaleQuantity());
        doc.setStock(goods.getStock());
        doc.setScore(goods.getScore());
        doc.setImg1(goods.getImg1());
        return doc;
    }
}

package com.chesnake.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chesnake.common.PageResult;
import com.chesnake.entity.Goods;
import com.chesnake.mapper.GoodsMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 搜索降级测试：ES 不可用时 MySQL LIKE 兜底返回
 */
class GoodsServiceSearchTest {

    private final GoodsMapper goodsMapper = mock(GoodsMapper.class);
    private final StringRedisTemplate stringRedis = mock(StringRedisTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CacheDoubleDeleteService doubleDelete = mock(CacheDoubleDeleteService.class);
    private final GoodsService service = new GoodsService(goodsMapper, stringRedis, objectMapper, doubleDelete);

    @Test
    void searchByMysql_returnsPageResult() {
        Page<Goods> page = new Page<>(1, 20);
        Goods goods = new Goods();
        goods.setId(1L);
        goods.setName("薯片");
        page.setRecords(List.of(goods));
        page.setTotal(1);
        when(goodsMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResult<Goods> result = service.searchByMysql("薯片", 1, 20, "price", "asc");

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).getName()).isEqualTo("薯片");
    }
}

package com.chesnake.dto;

/**
 * 商品查询条件（关键词/品牌/类型/产地/排序/分页）
 */
public record GoodsQueryDTO(
        String keyword,
        String brand,
        String type,
        String origin,
        String field,
        String direction,
        long page,
        long size
) {
    public GoodsQueryDTO {
        if (page <= 0) page = 1;
        if (size <= 0 || size > 100) size = 20;
        if (field == null || field.isBlank()) field = "score";
        if (direction == null || direction.isBlank()) direction = "desc";
    }
}

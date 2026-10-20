package com.chesnake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 管理员新增/修改商品
 */
public record GoodsSaveDTO(
        Long id,
        @NotBlank(message = "商品名不能为空") String name,
        @NotBlank(message = "类型不能为空") String type,
        @NotBlank(message = "品牌不能为空") String brand,
        @NotBlank(message = "产地不能为空") String origin,
        @NotNull(message = "净含量不能为空") Integer netContent,
        @NotNull(message = "价格不能为空") BigDecimal price,
        Integer stock,
        Integer saleQuantity,
        String img1,
        String img2
) {
}

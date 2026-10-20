package com.chesnake.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartAddDTO(
        @NotNull(message = "商品ID不能为空") Long goodId,
        @Positive(message = "数量必须为正数") Integer amount
) {
}

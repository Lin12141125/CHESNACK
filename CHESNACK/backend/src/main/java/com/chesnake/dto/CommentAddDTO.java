package com.chesnake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 提交商品评价
 */
public record CommentAddDTO(
        @NotNull(message = "订单ID不能为空") Long orderId,
        @NotBlank(message = "评价内容不能为空") String content,
        Integer level
) {
}

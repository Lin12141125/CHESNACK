package com.chesnake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentReplyDTO(
        @NotNull(message = "商品ID不能为空") Long goodsId,
        @NotNull(message = "被回复的评论ID不能为空") Long commentId,
        @NotBlank(message = "回复内容不能为空") String content
) {
}

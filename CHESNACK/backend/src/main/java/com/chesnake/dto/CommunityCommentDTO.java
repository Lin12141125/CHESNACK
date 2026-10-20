package com.chesnake.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 社区评论
 */
public record CommunityCommentDTO(
        @NotNull(message = "帖子ID不能为空") Long communityId,
        Long pid,
        @NotBlank(message = "评论内容不能为空") String content
) {
}

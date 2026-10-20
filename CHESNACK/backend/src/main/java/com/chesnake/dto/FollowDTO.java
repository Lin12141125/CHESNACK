package com.chesnake.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 关注
 */
public record FollowDTO(
        @NotBlank(message = "被关注者用户名不能为空") String username
) {
}

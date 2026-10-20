package com.chesnake.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 发帖
 */
public record PostDTO(
        @NotBlank(message = "标题不能为空") String title,
        String content
) {
}

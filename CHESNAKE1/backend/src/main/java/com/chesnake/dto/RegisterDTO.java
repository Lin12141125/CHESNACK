package com.chesnake.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDTO(
        @NotBlank(message = "账号不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "昵称不能为空") String username,
        @NotBlank(message = "密码不能为空") String password,
        /** 1-管理员 2-用户 */
        Integer role
) {
}

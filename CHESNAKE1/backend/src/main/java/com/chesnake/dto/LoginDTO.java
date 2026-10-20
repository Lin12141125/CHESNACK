package com.chesnake.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "账号不能为空") @Email(message = "邮箱格式不正确") String email,
        @NotBlank(message = "密码不能为空") String password,
        Integer role
) {
}

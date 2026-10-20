package com.chesnake.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PayDTO(
        @NotEmpty(message = "请选择要结算的商品") List<Long> goodIds
) {
}

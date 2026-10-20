package com.chesnake.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车展示 VO（关联商品信息）
 */
@Data
public class CartVO {
    private Long id;
    private String email;
    private Long goodId;
    private Integer amount;
    private BigDecimal price;
    private String name;
    private BigDecimal unitPrice;
    private String img1;
}

package com.chesnake.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单展示 VO（后端 join 商品信息，避免前端 N+1 查询）
 */
@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private String email;
    private Long goodId;
    private Integer amount;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime createTime;
    private String goodsName;
    private String goodsImg;
    /** 是否已评价（EXISTS 子查询回填，前端据此展示「已评价」） */
    private Boolean evaluated;
}

package com.chesnake.mq;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 下单支付异步消息（幂等由 orderNo 唯一索引 + 消费幂等表兜底）
 */
public record OrderMessage(String orderNo, String email, List<Item> items) implements Serializable {
    public record Item(Long goodId, Integer amount, BigDecimal price) implements Serializable {
    }
}

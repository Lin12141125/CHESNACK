package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 消费幂等记录：消费者处理前抢占，msgId+consumer 唯一索引兜底重复消费
 */
@Data
@TableName("mq_processed")
public class MqProcessed {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String msgId;
    private String consumer;
    private LocalDateTime createTime;
}

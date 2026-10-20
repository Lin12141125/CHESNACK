package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 本地消息表（生产者可靠性：发送前落库 → Confirm 回调更新状态 → 补偿任务重投）
 */
@Data
@TableName("mq_outbox")
public class MqOutbox {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 消息唯一 ID（UUID） */
    private String msgId;
    private String exchange;
    private String routingKey;
    /** 消息体 JSON */
    private String payload;
    /** 0-待确认 1-已确认 2-确认失败 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

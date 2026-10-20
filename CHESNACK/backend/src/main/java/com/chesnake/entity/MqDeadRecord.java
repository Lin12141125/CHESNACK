package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 死信补偿记录：重试耗尽仍失败的消息落表，供补偿任务重新投递或人工处理
 */
@Data
@TableName("mq_dead_record")
public class MqDeadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String msgId;
    /** 原业务队列名 */
    private String queue;
    private String body;
    private String error;
    /** 0-待处理 1-已处理 */
    private Integer status;
    private LocalDateTime createTime;
}

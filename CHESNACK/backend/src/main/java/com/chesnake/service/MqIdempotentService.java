package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chesnake.entity.MqProcessed;
import com.chesnake.mapper.MqProcessedMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 消费幂等（优化6）：
 * 幂等记录必须与业务逻辑在同一事务内写入——业务失败回滚时幂等记录一并回滚，
 * 保证死信重投/补偿重投后可以重新处理，而不是被「残留幂等记录」误拦截。
 */
@Service
@RequiredArgsConstructor
public class MqIdempotentService {

    private final MqProcessedMapper processedMapper;

    /** 是否已处理过（重复投递/重复消费探测） */
    public boolean isProcessed(String msgId, String consumer) {
        if (msgId == null || msgId.isBlank()) {
            return false;
        }
        Long count = processedMapper.selectCount(new LambdaQueryWrapper<MqProcessed>()
                .eq(MqProcessed::getMsgId, msgId)
                .eq(MqProcessed::getConsumer, consumer));
        return count != null && count > 0;
    }

    /**
     * 事务内抢占幂等记录；DuplicateKey 说明并发重复处理，抛异常触发事务回滚
     */
    public void acquire(String msgId, String consumer) {
        if (msgId == null || msgId.isBlank()) {
            return; // 无 msgId 的旧消息不拦截
        }
        try {
            MqProcessed record = new MqProcessed();
            record.setMsgId(msgId);
            record.setConsumer(consumer);
            processedMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("消息已被并发处理: " + msgId);
        }
    }

    /**
     * 释放幂等记录（无事务场景下业务落库失败时调用，保证重投可以重试）
     */
    public void release(String msgId, String consumer) {
        if (msgId == null || msgId.isBlank()) {
            return;
        }
        processedMapper.delete(new LambdaQueryWrapper<MqProcessed>()
                .eq(MqProcessed::getMsgId, msgId)
                .eq(MqProcessed::getConsumer, consumer));
    }
}

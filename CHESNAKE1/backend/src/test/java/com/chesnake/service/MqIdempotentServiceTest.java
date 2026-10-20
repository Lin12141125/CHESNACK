package com.chesnake.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.chesnake.entity.MqProcessed;
import com.chesnake.mapper.MqProcessedMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 消费幂等测试：重复探测、事务内抢占、DuplicateKey 触发回滚信号、释放
 */
class MqIdempotentServiceTest {

    private final MqProcessedMapper mapper = mock(MqProcessedMapper.class);
    private final MqIdempotentService service = new MqIdempotentService(mapper);

    @Test
    void isProcessed_returnsTrueWhenRecordExists() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThat(service.isProcessed("msg-1", "ORDER")).isTrue();
    }

    @Test
    void isProcessed_returnsFalseWhenNoRecord() {
        when(mapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        assertThat(service.isProcessed("msg-1", "ORDER")).isFalse();
    }

    @Test
    void isProcessed_blankMsgId_returnsFalse() {
        assertThat(service.isProcessed("", "ORDER")).isFalse();
    }

    @Test
    void acquire_insertsRecord() {
        service.acquire("msg-1", "ORDER");

        verify(mapper).insert(any(MqProcessed.class));
    }

    @Test
    void acquire_duplicateKey_throwsToRollbackTransaction() {
        when(mapper.insert(any(MqProcessed.class))).thenThrow(new DuplicateKeyException("dup"));

        assertThatThrownBy(() -> service.acquire("msg-1", "ORDER"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void release_deletesRecord() {
        service.release("msg-1", "ORDER");

        verify(mapper).delete(any(Wrapper.class));
    }
}

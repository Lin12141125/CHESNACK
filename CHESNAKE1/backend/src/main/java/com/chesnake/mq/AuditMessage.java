package com.chesnake.mq;

import java.io.Serializable;

/**
 * 管理员操作审计消息
 */
public record AuditMessage(String username, String action, String detail) implements Serializable {
}

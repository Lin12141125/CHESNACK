package com.chesnake.mq;

import java.io.Serializable;

/**
 * 站内通知消息
 */
public record NotifyMessage(String email, String title, String content) implements Serializable {
}

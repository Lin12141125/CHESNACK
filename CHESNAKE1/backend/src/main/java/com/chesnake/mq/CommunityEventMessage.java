package com.chesnake.mq;

import java.io.Serializable;

/**
 * 社区事件消息（发帖/评论，异步通知相关用户）
 */
public record CommunityEventMessage(String type, Long postId, Long commentId,
                                    String fromUser, String toEmail, String text) implements Serializable {
    public static final String TYPE_POST = "post";
    public static final String TYPE_COMMENT = "comment";
}

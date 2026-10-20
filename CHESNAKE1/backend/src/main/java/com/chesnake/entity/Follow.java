package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 被关注者 email */
    private String account;
    /** 关注者用户名 */
    private String followee;
    /** 关注者 email（用于发帖事件通知/WebSocket 推送） */
    private String followeeEmail;
    private LocalDateTime createTime;
}

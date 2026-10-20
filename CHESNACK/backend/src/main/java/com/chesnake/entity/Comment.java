package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String content;
    private LocalDateTime sendTime;
    private Long goodsId;
    private Long orderId;
    private Long pid;
    private Integer level;
    private Integer reported;

    /** 二级回复列表（非表字段） */
    @TableField(exist = false)
    private List<Comment> replies;
}

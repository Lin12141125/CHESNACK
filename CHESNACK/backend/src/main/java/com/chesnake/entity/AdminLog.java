package com.chesnake.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_log")
public class AdminLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String action;
    private String detail;
    private LocalDateTime createTime;
}

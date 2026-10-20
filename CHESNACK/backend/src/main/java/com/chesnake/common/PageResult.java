package com.chesnake.common;

import java.util.List;

/**
 * 轻量分页结果（缓存序列化用，避免把 MyBatis-Plus Page 的内部字段写入 Redis）
 */
public record PageResult<T>(List<T> records, long total) {
}

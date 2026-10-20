package com.chesnake.common;

/**
 * 缓存 Key 与常量定义
 */
public final class CacheConsts {
    private CacheConsts() {}

    /** 商品详情 key 前缀 */
    public static final String GOODS_DETAIL = "cache:goods:detail:";
    /** 商品列表 key 前缀 */
    public static final String GOODS_LIST = "cache:goods:list:";
    /** 击穿保护互斥锁 key 前缀 */
    public static final String GOODS_LOCK = "lock:goods:";
    /** JWT 黑名单 key 前缀（主动吊销） */
    public static final String JWT_BLACKLIST = "auth:blacklist:";
    /** 空值占位符（防穿透） */
    public static final String NULL_PLACEHOLDER = "__NULL__";

    /** 详情缓存基础 TTL（防雪崩：TTL=基础值+随机值） */
    public static final long GOODS_DETAIL_TTL_MINUTES = 30;
    public static final long GOODS_DETAIL_TTL_RANDOM_MINUTES = 10;
    /** 列表缓存 TTL */
    public static final long GOODS_LIST_TTL_MINUTES = 5;
    public static final long GOODS_LIST_TTL_RANDOM_MINUTES = 3;
    /** 空值缓存 TTL（防穿透：短期缓存空结果） */
    public static final long NULL_TTL_MINUTES = 5;
    /** 互斥锁 TTL */
    public static final long LOCK_TTL_SECONDS = 10;
}
